package com.vism.gethlibrary;

import android.content.Context;
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
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

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
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(4300000);
    private Web3j web3;
    private Context context;
    Credentials credentials = null;
    public VismGeth(){}
    public VismGeth(Context context){
        this.context = context;
        web3 = Web3jFactory.build(new HttpService("https://wallet.parity.io/"));
    }

    /**
     *   创建轻钱包, 并将私钥保存在/data/data/package.name/files/路径下
     * @param password 用户设置的密码
     * @return boolean 钱包是否创建成功
     */
    public boolean createWallet(String password){
        try {
            WalletUtils.generateLightNewWalletFile(password,context.getFilesDir());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,"错误信息:"+e.getMessage());
            return false;
        } catch (NoSuchProviderException e) {
            Log.e(TAG,"错误信息:"+e.getMessage());
            return false;
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG,"错误信息:"+e.getMessage());
            return false;
        } catch (CipherException e) {
            Log.e(TAG,"错误信息:"+e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG,"错误信息:"+e.getMessage());
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
            FileTools.savePrivateFiles(context,keystore,filename);
            credentials = WalletUtils.loadCredentials(password,context.getFilesDir()+"/"+filename);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            return null;
        }
        return credentials;
    }

    /**
     *   获取已经导入的账户凭证Credentials;
     *   通过Credentials的getAddress()方法可以获得钱包地址Address
     * @return Credentials
     */
    public Credentials getCredentials(){
        return credentials;
    }

    /**
     *   ETH 进行转账,并返回交易哈希值 ;如果返回值为null，交易未广播成功
     * @param to
     * @param gasPrice
     * @param balance
     * @return
     */
    public String sendETH(String to, String gasPrice,String balance){
        String transaction_hash = "";
        try {
            // get the next available nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            // create transaction
            RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                    nonce,
                    new BigInteger(gasPrice),
                    GAS_LIMIT,
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
     *   根据代币合约地址， 进行代币转账
     * @param to
     * @param gasPrice
     * @param balance
     * @param contractAddress
     * @return 交易哈希值
     */
    public String sendTokenByContract(String to, String gasPrice, String balance, String contractAddress){
        String transaction_hash = "";
        try {
            // get the next available nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            // get Contract function "transfer"
            Function tranferFunc = ContractTools.transfer(to,balance);
            String encodedFunction = FunctionEncoder.encode(tranferFunc);

            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    new BigInteger(gasPrice),
                    GAS_LIMIT,
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
}
