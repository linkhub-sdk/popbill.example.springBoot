package com.popbill.example.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.AccountCheckInfo;
import com.popbill.api.AccountCheckService;
import com.popbill.api.ChargeInfo;
import com.popbill.api.DepositorCheckInfo;
import com.popbill.api.PopbillException;

@Controller
@RequestMapping("AccountCheckService")
public class AccountCheckServiceController {

    @Autowired
    private AccountCheckService accountCheckService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "AccountCheck/index";
    }

    @RequestMapping(value = "checkAccountInfo", method = RequestMethod.GET)
    public String checkAccountInfo(Model m) {
        /*
         * 1건의 예금주성명을 조회합니다. 
         * - https://docs.popbill.com/accountcheck/java/api#CheckAccountInfo
         */

        // 기관코드
        String BankCode = "0004";

        // 계좌번호
        String AccountNumber = "9432451175834";

        try {

            AccountCheckInfo accountInfo = accountCheckService.CheckAccountInfo(testCorpNum, BankCode, AccountNumber, testUserID);

            m.addAttribute("AccountInfo", accountInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "AccountCheck/checkAccountInfo";
    }

    @RequestMapping(value = "checkDepositorInfo", method = RequestMethod.GET)
    public String checkDepositorInfo(Model m) {
        /*
         * 1건의 예금주실명을 조회합니다.
         * - https://docs.popbill.com/accountcheck/java/api#CheckDepositorInfo
         */

        // 기관코드
        String BankCode = "0004";
        
        // 계좌번호
        String AccountNumber = "9432451175834";
        
        //등록번호 유형
        String IdentityNumType ="P";
        
        //등록번호
        String IdentityNum = "901112";

        try {

            DepositorCheckInfo depositorCheckInfo = accountCheckService.CheckDepositorInfo(testCorpNum, BankCode, AccountNumber, IdentityNumType, IdentityNum);

            m.addAttribute("DepositorCheckInfo", depositorCheckInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "AccountCheck/checkDepositorInfo";
    }
    
    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /*
         * 예금주조회시 과금되는 포인트 단가를 확인합니다. 
         * - https://docs.popbill.com/accountcheck/java/api#GetUnitCost
         */

        // 서비스 유형, 계좌성명조회일 때는 "성명"을 입력하고, 계좌실명조회일 때는 "실명"을 입력합니다.
        String ServiceType = "성명";

        try {

            float unitCost = accountCheckService.getUnitCost(testCorpNum,ServiceType);

            m.addAttribute("Result", unitCost);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /*
         * 예금주조회 API 서비스 과금정보를 확인합니다. 
         * - https://docs.popbill.com/accountcheck/java/api#GetChargeInfo
         */

        // 서비스 유형, 계좌성명조회일 때는 "성명"을 입력하고, 계좌실명조회일 때는 "실명"을 입력합니다.
        String ServiceType = "성명";

        try {
            ChargeInfo chrgInfo = accountCheckService.getChargeInfo(testCorpNum, ServiceType);
            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

}
