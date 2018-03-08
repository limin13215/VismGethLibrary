package com.vism.gethlibrary;

import android.content.Context;
import android.util.Log;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by Administrator on 2018-3-7.
 */

public class VismGeth {
    private final static String TAG ="VismGethLibrary";
    private Web3j web3;
    private Context context;
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
