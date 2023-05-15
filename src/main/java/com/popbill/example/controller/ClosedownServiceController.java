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
    private String testCorpNum = "1234567890";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Closedown/index";
    }

    @RequestMapping(value = "checkCorpNum", method = RequestMethod.GET)
    public String checkCorpNum(@RequestParam(required = false) String CorpNum, Model m) {
        /**
         * 사업자번호 1건에 대한 휴폐업정보를 확인합니다.
         * - https://developers.popbill.com/reference/closedown/java/api/check#CheckCorpNum
         */

        if (CorpNum != null && !CorpNum.equals("")) {

            try {
                CorpState corpState = closedownService.CheckCorpNum(testCorpNum, CorpNum);

                m.addAttribute("CorpState", corpState);

            } catch (PopbillException e) {
                m.addAttribute("Exception", e);
                return "exception";
            }

        } else {


        }
        return "Closedown/checkCorpNum";
    }

    @RequestMapping(value = "checkCorpNums", method = RequestMethod.GET)
    public String checkCorpNums(Model m) {
        /**
         * 다수건의 사업자번호에 대한 휴폐업정보를 확인합니다. (최대 1,000건)
         * - https://developers.popbill.com/reference/closedown/java/api/check#CheckCorpNums
         */

        // 조회할 사업자번호 배열, 최대 1000건
        String[] CorpNumList = new String[]{"1234567890", "6798700433"};

        try {

            CorpState[] corpStates = closedownService.CheckCorpNum(testCorpNum, CorpNumList);

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
         * 휴폐업 조회시 과금되는 포인트 단가를 확인합니다.
         * - https://developers.popbill.com/reference/closedown/java/api/point#GetUnitCost
         */

        try {

            float unitCost = closedownService.getUnitCost(testCorpNum);

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
         * 팝빌 휴폐업조회 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/closedown/java/api/point#GetChargeInfo
         */

        try {
            ChargeInfo chrgInfo = closedownService.getChargeInfo(testCorpNum);
            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

}
