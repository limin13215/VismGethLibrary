package com.vism.gethlibrary;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Administrator on 2018-3-7.
 */

public class VismGeth {
    private static final  String TAG ="VismGethLibrary";
    private static final String DEFAULT_GASLIMIT_TOKENS = "90000";
    private static final String DEFAULT_GASLIMIT_ETH = "25200";
    private static final String ETHERSCAN_API_KEY= "QVPD417PDAIRRF8RGPEDQGMF6G3GE74BPG";
    private static final String GetTokenBalanceUrl = "https://api.etherscan.io/api?module=account&action=tokenbalance&contractaddress=CONTRACT_ADDRESS&address=WALLET_ADDRESS&tag=latest&apikey=YourApiKeyToken";

    public static final int HANDLER_MSG_WHAT = 0x1234;
    public static final String HANDLER_MSG_BALANCE = "TokenBalance";
    private Web3j web3;
    private Credentials credentials = null;

    private Context context;
    public Handler myHandler = null;
    public String mKeystoreFilePath = null;

    public VismGeth(){}
    public VismGeth(Context context){
        this.context = context;
        web3 = Web3jFactory.build(new HttpService("https://wallet.parity.io/"));
    }
    public VismGeth(Context context, Handler mHandler){
        this.context = context;
        web3 = Web3jFactory.build(new HttpService("https://wallet.parity.io/"));
        myHandler = mHandler;
    }

    /**
     *   创建轻钱包, 并将私钥保存在/data/data/package.name/files/路径下
     * @param password 用户设置的密码
     * @return boolean 钱包是否创建成功
     */
    public boolean createWallet(String password){
        try {
            String jsonName = WalletUtils.generateLightNewWalletFile(password,context.getFilesDir());
            mKeystoreFilePath = jsonName;
            Log.e(TAG,"keystore file`s name:  "+ jsonName);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,"createWallet() ERROR:"+e.getMessage());
            return false;
        } catch (NoSuchProviderException e) {
            Log.e(TAG,"createWallet() ERROR:"+e.getMessage());
            return false;
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG,"createWallet() ERROR:"+e.getMessage());
            return false;
        } catch (CipherException e) {
            Log.e(TAG,"createWallet() ERROR:"+e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG,"createWallet() ERROR:"+e.getMessage());
            return false;
        }
        return true;
    }

    /**
     *  导入钱包，并且返回钱包凭证; 如果返回值为null, 导入钱包失败;
     * @param password
     * @param keystore
     * @param filename
     * @return
     */
    public Credentials importWallet(String password, String keystore, String filename){
        try {
            mKeystoreFilePath = filename;
            FileTools.savePrivateFiles(context,keystore,filename);
            credentials = WalletUtils.loadCredentials(password,context.getFilesDir()+"/"+filename);
        } catch (IOException e) {
            Log.e(TAG,"importWallet(1,2,3) ERROR:"+e.getMessage());
            return null;
        } catch (CipherException e) {
            Log.e(TAG,"importWallet(1,2,3) ERROR:"+e.getMessage());
            return null;
        }
        return credentials;
    }

    /**
     *   导入自己的钱包
     * @param password
     * @param filename
     * @return
     */
    public Credentials importWallet(String password, String filename){
        try {
            mKeystoreFilePath = filename;
            credentials = WalletUtils.loadCredentials(password,context.getFilesDir()+"/"+filename);
        } catch (IOException e) {
            Log.e(TAG,"importWallet(1,2) ERROR:"+e.getMessage());
            return null;
        } catch (CipherException e) {
            Log.e(TAG,"importWallet(1,2) ERROR:"+e.getMessage());
            return null;
        }
        return credentials;
    }

    public String getKeyStoreByName(){
        String result = "";
        try {
            FileInputStream inputStream = new FileInputStream(context.getFilesDir()+"/"+mKeystoreFilePath);
            byte temp[] = new byte[1024];
            StringBuilder sb = new StringBuilder("");
            int len = 0;
            while ((len = inputStream.read(temp)) > 0){
                sb.append(new String(temp, 0, len));
            }
            inputStream.close();
            result = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){

        }
        return result;
    }

    /**
     *   获取已经导入的账户凭证Credentials;
     *   通过Credentials的getAddress()方法可以获得钱包地址Address
     * @return Credentials
     */
    private Credentials getCredentials(){
        return credentials;
    }

    /**
     *   ETH 进行转账,并返回交易哈希值 ;如果返回值为null，交易未广播成功
     *
     *   注意: 当gasLimit=""或者gasLimit=null ，自动估算gasLimit， 否则，进行自定义转账;
     * @param to
     * @param gasPrice
     * @param balance
     * @return
     */
    public String sendETH(String to, String gasPrice,String gasLimit,String balance){
        String transaction_hash = "";
        String calculationGas = "";
        try {
            // get the next available nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            if (gasLimit!=null && gasLimit.length()!=0){
                calculationGas = gasLimit;
            }else {
                calculationGas = DEFAULT_GASLIMIT_ETH;
            }

            // create transaction
            RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                    nonce,
                    new BigInteger(gasPrice),
                    new BigInteger(calculationGas),
                    to,
                    new BigInteger(balance));
            // sign & send our transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
            transaction_hash = ethSendTransaction.getTransactionHash();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return transaction_hash;
    }

    /**
     *   根据代币合约地址， 进行代币转账;
     *         当gasLimit=""或者gasLimit=null ，自动估算gasLimit， 否则，进行自定义转账;
     *
     * @param to
     * @param gasPrice
     * @param balance
     * @param contractAddress
     * @return 交易哈希值
     */
    public String sendTokenByContract(String to, String gasPrice, String gasLimit, String balance, String contractAddress){
        String transaction_hash = "";
        String estimateGas = "";
        try {
            // get the next available nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            // get Contract function "transfer"
            Function tranferFunc = ContractTools.transfer(to,balance);
            String encodedFunction = FunctionEncoder.encode(tranferFunc);

            if (gasLimit!=null && gasLimit.length()!=0){
                //自定义gas
                estimateGas = gasLimit;
            }else {
                //gasLimit=null || gasLimit=""，估值gas大小
                estimateGas = getEstimateGasLimit(
                                credentials.getAddress(),
                                nonce,
                                new BigInteger(gasPrice),
                                new BigInteger(DEFAULT_GASLIMIT_TOKENS),
                                contractAddress,
                                encodedFunction
                                );
            }

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    new BigInteger(gasPrice),
                    new BigInteger(estimateGas),
                    contractAddress,
                    encodedFunction);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction transactionResponse = web3.ethSendRawTransaction(hexValue).sendAsync().get();

            transaction_hash = transactionResponse.getTransactionHash();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return transaction_hash;
    }

    /**
     *    获取gasLimit
     *
     * @param contractAddress
     * @return gasLimit
     */
    public String getGasLimit(String contractAddress){
        String result = null;
        String from = "";
        if (credentials!=null){
            from = credentials.getAddress();
        }else {
            from = "0x909Aa5ad1e3BB554347c763c06b8681E9C5A9694";
        }
        // get the next available nonce
        try {
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            // get Contract function "transfer"
            Function tranferFunc = ContractTools.transfer("0xe52307ebd18d20ab81f2abf582f1f0007ff658f0","1");
            String encodedFunction = FunctionEncoder.encode(tranferFunc);
            result = getEstimateGasLimit(from,
                                        nonce,
                                        new BigInteger("6000000000"),
                                        new BigInteger(DEFAULT_GASLIMIT_TOKENS),
                                        contractAddress,
                                        encodedFunction
                                        );
        } catch (IOException e) {
            //TODO
            return DEFAULT_GASLIMIT_TOKENS;
        }
        return result;
    }

    /**
     *   封装以太坊估算gasLimit方法:
     *        计算代币进行合约转账时，消耗的gas数量
     * @param address
     * @param nonce
     * @param gasPrice
     * @param gasLimit
     * @param contractAddress
     * @param encodedFunction
     * @return String
     */
    private String getEstimateGasLimit(String address, BigInteger nonce,
                                      BigInteger gasPrice, BigInteger gasLimit,
                                      String contractAddress, String encodedFunction) {
        String result ="";
        Transaction transaction =
                Transaction.createFunctionCallTransaction(address,nonce,gasPrice,gasLimit,
                                                          contractAddress,encodedFunction);
        try {
            result = web3.ethEstimateGas(transaction).send().getAmountUsed().toString();
            int num = Integer.valueOf(result) + 20000;
            result = ""+num;
        } catch (IOException e) {
            return DEFAULT_GASLIMIT_TOKENS;
        }
        return result;
    }

    /**
     *  根据钱包地址，获取ETH数量
     * @param address
     * @return BigInteger
     */
    public BigInteger getEthBalance(String address){
        BigInteger balance = null;
        try {
            balance = web3.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        } catch (IOException e) {
            Log.e(TAG ,"错误提示信息:"+e.getMessage());
            return balance;
        }
        return balance;
    }

    /**
     *   通过代币合约地址，查询代币数量
     * @param address
     * @param contractAddress
     * @return
     */
    public void getBalanceAsyncHandle(String address,String contractAddress){
        final String url = GetTokenBalanceUrl
                     .replace("CONTRACT_ADDRESS",contractAddress)
                     .replace("WALLET_ADDRESS",address)
                     .replace("YourApiKeyToken",ETHERSCAN_API_KEY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // {"status":"1","message":"OK","result":"135499"}
                HttpUtils httpUtils = new HttpUtils();
                httpUtils.setMyCallBack(new HttpUtils.HttpUtilsCallBack() {
                    @Override
                    public void onRequestComplete(String result) {
                        String balance = JsonTools.parseJSONWithJSONObject(result);
                        //Log.e(TAG,"resultJson解析111: balance="+balance);
                        Message msg = myHandler.obtainMessage();
                        msg.what = HANDLER_MSG_WHAT;
                        Bundle bundle = new Bundle();
                        bundle.putString(HANDLER_MSG_BALANCE, balance);
                        msg.setData(bundle);
                        myHandler.sendMessage(msg);
                    }
                });
                httpUtils.doGet(url);
            }
        }).start();
    }
}
