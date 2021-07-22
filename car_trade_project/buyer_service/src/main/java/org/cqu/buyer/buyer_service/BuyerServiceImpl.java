package org.cqu.buyer.buyer_service;

import org.apache.dubbo.config.annotation.Service;
import org.cqu.buyer_api.BuyerService;
import org.cqu.dto.ResultInfo;
import org.cqu.mapper.BuyerMapper;
import org.cqu.mapper.CartMapper;
import org.cqu.pojo.Buyer;
import org.cqu.pojo.Cart;
import org.cqu.pojo.CartExample;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuyerServiceImpl implements BuyerService {
    @Autowired
    private BuyerMapper buyerMapper;
    @Autowired
    private CartMapper cartMapper;

    private ResultInfo<Buyer> result = new ResultInfo<>();

    @Override
    public Buyer findBuyerByTel(String BuyerTel) {
        return buyerMapper.selectByPrimaryKey(BuyerTel);
    }

    @Override
    public String getBuyerName(String BuyerTel) {
        Buyer buyer = buyerMapper.selectByPrimaryKey(BuyerTel);
        if(buyer != null){
            return buyer.getBname();
        }
        return "NotFound";
    }

    @Override
    public ResultInfo<Buyer> login(String btel, String bpassword) {
        String token = "";
        System.out.println(btel + " " + bpassword);
        try {
            Buyer userExist = buyerMapper.selectByPrimaryKey(btel);
            if (userExist == null) {
                result.setMsg("Buyer doesn't exist! Please retry!");
                result.setSuccess(false);
                result.setToken(token);
                result.setId("null");
            } else if (userExist.getBpassword().equals(bpassword)){
                result.setMsg("Log in succeed!");
                result.setId(btel);
                result.setSuccess(true);
                token = result.generate_token(btel);
                result.setToken(token);
            } else {
                result.setMsg("password wrong! Please retry!");
                result.setSuccess(false);
                result.setToken(token);
                result.setId(btel);
            }
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setSuccess(false);
            result.setToken(token);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ResultInfo<Buyer> register(String Username, String Gender, String Address, String Tel, String Password) {
        Buyer buyer = new Buyer();
        buyer.setBpassword(Password);
        buyer.setBtel(Tel);
        buyer.setBaddress(Address);
        buyer.setBname(Username);
        if(Gender.equals("Male")){
            buyer.setBgender(1);   // 1 for male and 0 for female
        } else {
            buyer.setBgender(0);
        }
        result.setToken("null");
        try {
            /**
             * 手机号码作为登录的方式。如果手机号码已经被使用则注册失败：
             * 在数据库中查找是否有该手机号的用户，若无则注册成功。
             * */
            Buyer name = buyerMapper.selectByPrimaryKey(buyer.getBtel());
            if (name != null) {
                result.setMsg("This phone number is used, please try another. ");
                result.setSuccess(false);
            } else {
                buyerMapper.insert(buyer);
                result.setMsg("Register success!");
                result.setSuccess(true);
            }
        } catch (Exception e) {
            result.setMsg(e.getMessage());
            result.setSuccess(false);
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ResultInfo<Buyer> update(String btel, String baddress, String bname){
        Buyer userExist = buyerMapper.selectByPrimaryKey(btel);
        userExist.setBaddress(baddress);
        userExist.setBname(bname);
        buyerMapper.updateByPrimaryKey(userExist);
        result.setMsg("update succeed!");
        result.setSuccess(true);
        return result;
    }

    @Override
    public Map<String, String> getInfo(String btel){
        Map<String, String> buyer_info = new HashMap<String, String>();
        Buyer buyer = buyerMapper.selectByPrimaryKey(btel);
        buyer_info.put("tel", buyer.getBtel());
        buyer_info.put("name", buyer.getBname());
        buyer_info.put("address",buyer.getBaddress());
        buyer_info.put("gender",String.valueOf(buyer.getBgender()));
        buyer_info.put("vip", String.valueOf(buyer.getBvip()));
        return buyer_info;
    }



    @Override
    public void updateIcon(String btel, String pic_url) {
        Buyer userExist = buyerMapper.selectByPrimaryKey(btel);
        userExist.setBicon(pic_url);
        buyerMapper.updateByPrimaryKey(userExist);
    }

    @Override
    public ResultInfo<Cart> getHistoryOrder(String btel) {
        ResultInfo<Cart> cart_res = new ResultInfo<>();
        CartExample ce = new CartExample();
        CartExample.Criteria criteria = new CartExample.Criteria();
        criteria.andBtelEqualTo(btel);
        List<Cart> history_info = cartMapper.selectByExample(ce);
        cart_res.setResult_list(history_info);
        return cart_res;
    }
}
