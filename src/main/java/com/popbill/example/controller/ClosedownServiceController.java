/**
  * 팝빌 사업자등록상태조회 (휴폐업조회) API Java SDK SpringBoot Example
  *
  * SpringBoot 연동 튜토리얼 안내 : https://developers.popbill.com/guide/closedown/java/getting-started/tutorial?fwn=springboot
  * 연동 기술지원 연락처 : 1600-9854
  * 연동 기술지원 이메일 : code@linkhubcorp.com
  *
  */
package com.popbill.example.controller;

import java.util.Locale;
import com.popbill.api.ChargeInfo;
import com.popbill.api.CloseDownService;
import com.popbill.api.CorpState;
import com.popbill.api.PopbillException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("CloseDownService")
public class ClosedownServiceController {

    @Autowired
    private CloseDownService closedownService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Closedown/index";
    }

    @RequestMapping(value = "checkCorpNum", method = RequestMethod.GET)
    public String checkCorpNum(@RequestParam(required = false) String CheckCorpNum, Model m) {
        /**
         * 사업자번호 1건에 대해 실시간으로 사업자등록상태를 확인합니다.
         * 팝빌 서비스의 안정적인 제공을 위하여 동시호출이 제한될 수 있습니다.
         * 동시에 100건 이상 요청하는 경우 대량 조회를 이용하시는 것을 권장합니다.
         * - https://developers.popbill.com/reference/closedown/java/api/check#CheckCorpNum
         */

        if (CheckCorpNum != null && !CheckCorpNum.equals("")) {
            try {
                CorpState corpState = closedownService.CheckCorpNum(CorpNum, CheckCorpNum, UserID);
                m.addAttribute("CorpState", corpState);
            } catch (PopbillException e) {
                m.addAttribute("Exception", e);
                return "exception";
            }
        }

        return "Closedown/checkCorpNum";
    }

    @RequestMapping(value = "checkCorpNums", method = RequestMethod.GET)
    public String checkCorpNums(Model m) {
        /**
         * 다수건의 사업자번호에 대한 사업자등록상태 (휴폐업조회) 를 확인합니다. (최대 1,000건)
         * - https://developers.popbill.com/reference/closedown/java/api/check#CheckCorpNums
         */

        // 사업자번호 목록, 최대 1000건
        String[] CorpNumList = new String[]{"1234567890", "6798700433"};

        try {
            CorpState[] corpStates = closedownService.CheckCorpNum(CorpNum, CorpNumList, UserID);
            m.addAttribute("CorpStates", corpStates);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Closedown/checkCorpNums";
    }

    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /**
         * 사업자등록상태 조회시 과금되는 포인트 단가를 확인합니다.
         * - https://developers.popbill.com/reference/closedown/java/common-api/point#GetUnitCost
         */

        try {
            float unitCost = closedownService.getUnitCost(CorpNum, UserID);
            m.addAttribute("Result", unitCost);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /**
         * 팝빌 사업자등록상태조회 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/closedown/java/common-api/point#GetChargeInfo
         */

        try {
            ChargeInfo chrgInfo = closedownService.getChargeInfo(CorpNum, UserID);
            m.addAttribute("ChargeInfo", chrgInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

}
