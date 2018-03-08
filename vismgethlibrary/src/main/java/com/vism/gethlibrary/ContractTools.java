package com.vism.gethlibrary;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-3-8.
 */

public class ContractTools {
    /**
     *  获取合约的transfer方法
     * @param to
     * @param value
     * @return
     */
    public static Function transfer(String to, String value) {
        BigInteger balance = new BigInteger(value);
        List<Type> typeList = new ArrayList<>();
        typeList.add(new Address(to));
        typeList.add(new Uint256(balance));

        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
            @Override
            public java.lang.reflect.Type getType() {
                return super.getType();
            }
        };

        List<TypeReference<?>> typeReferenceList = new ArrayList<>();
        typeReferenceList.add(typeReference);
        return new Function(
                "transfer", typeList, typeReferenceList);
    }
}
