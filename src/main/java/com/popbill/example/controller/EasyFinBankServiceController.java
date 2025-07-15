/**
  * 팝빌 계좌조회 API Java SDK SpringBoot Example
  *
  * SpringBoot 연동 튜토리얼 안내 : https://developers.popbill.com/guide/easyfinbank/java/getting-started/tutorial?fwn=springboot
  * 연동 기술지원 연락처 : 1600-9854
  * 연동 기술지원 이메일 : code@linkhubcorp.com
  *
  */
package com.popbill.example.controller;

import java.util.Locale;
import com.popbill.api.ChargeInfo;
import com.popbill.api.EasyFinBankService;
import com.popbill.api.FlatRateState;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.easyfin.EasyFinBankAccount;
import com.popbill.api.easyfin.EasyFinBankAccountForm;
import com.popbill.api.easyfin.EasyFinBankJobState;
import com.popbill.api.easyfin.EasyFinBankSearchResult;
import com.popbill.api.easyfin.EasyFinBankSummary;
import com.popbill.api.easyfin.UpdateEasyFinBankAccountForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("EasyFinBankService")
public class EasyFinBankServiceController {

    @Autowired
    private EasyFinBankService easyFinBankService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "EasyFinBank/index";
    }

    @RequestMapping(value = "registBankAccount", method = RequestMethod.GET)
    public String registBankAccount(Model m) {
        /**
         * 계좌조회 서비스를 이용할 계좌를 팝빌에 등록합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#RegistBankAccount
         */

        // 계좌 정보
        EasyFinBankAccountForm bankInfo = new EasyFinBankAccountForm();

        // 은행 기관코드
        bankInfo.setBankCode("");

        // 계좌번호 하이픈('-') 제외
        bankInfo.setAccountNumber("");

        // 계좌 비밀번호
        bankInfo.setAccountPWD("");

        // 계좌유형, "법인" / "개인" 중 택 1
        bankInfo.setAccountType("");

        // 실명번호 ('-' 제외)
        // 계좌유형이 "법인"인 경우 : 사업자번호(10자리)
        // 계좌유형이 "개인"인 경우 : 예금주 생년월일 (6자리-YYMMDD)
        bankInfo.setIdentityNumber("");

        // 계좌 별칭
        bankInfo.setAccountName("");

        // 인터넷뱅킹 아이디 (국민은행 필수)
        bankInfo.setBankID("");

        // 조회전용 계정 아이디 (아이엠뱅크, 신한은행, 신협중앙회 필수)
        bankInfo.setFastID("");

        // 조회전용 계정 비밀번호 (아이엠뱅크, 신한은행, 신협중앙회 필수)
        bankInfo.setFastPWD("");

        // 정액제 이용할 개월수, 1~12 입력가능
        // - 미입력시 기본값 1개월 처리
        // - 파트너 과금방식의 경우 입력값에 관계없이 1개월 처리
        bankInfo.setUsePeriod(1);

        // 메모
        bankInfo.setMemo("");

        try {
            Response response = easyFinBankService.registBankAccount(CorpNum, bankInfo, UserID);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }


    @RequestMapping(value = "updateBankAccount", method = RequestMethod.GET)
    public String updateBankAccount(Model m) {
        /**
         * 팝빌에 등록된 계좌정보를 수정합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#UpdateBankAccount
         */

        // 은행 기관코드
        String BankCode = "";

        // 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        // 수정할 계좌 정보
        UpdateEasyFinBankAccountForm BankAccountInfo = new UpdateEasyFinBankAccountForm();

        // 계좌 비밀번호
        BankAccountInfo.setAccountPWD("");

        // 계좌 별칭
        BankAccountInfo.setAccountName("");

        // 인터넷뱅킹 아이디 (국민은행 필수)
        BankAccountInfo.setBankID("");

        // 조회전용 계정 아이디 (아이엠뱅크, 신한은행, 신협중앙회 필수)
        BankAccountInfo.setFastID("");

        // 조회전용 계정 비밀번호 (아이엠뱅크, 신한은행, 신협중앙회 필수)
        BankAccountInfo.setFastPWD("");

        // 메모
        BankAccountInfo.setMemo("");

        try {
            Response response = easyFinBankService.updateBankAccount(CorpNum, BankCode, AccountNumber, BankAccountInfo,
                    UserID);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getBankAccountInfo", method = RequestMethod.GET)
    public String getBankAccountInfo(Model m) {
       /**
        * 팝빌에 등록된 계좌 정보를 확인합니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#GetBankAccountInfo
        */

        // 은행 기관코드
        String BankCode = "";

        // 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        try {
            EasyFinBankAccount bankAccountInfo = easyFinBankService.getBankAccountInfo(CorpNum, BankCode, AccountNumber,
                    UserID);
            m.addAttribute("Account", bankAccountInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/GetBankAccount";
    }

   @RequestMapping(value = "listBankAccount", method = RequestMethod.GET)
    public String listBankAccount(Model m) {
       /**
        * 팝빌에 등록된 계좌정보 목록을 반환합니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#ListBankAccount
        */

        try {
            EasyFinBankAccount[] bankList = easyFinBankService.listBankAccount(CorpNum, UserID);
            m.addAttribute("BankAccountList", bankList);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/ListBankAccount";
    }

   @RequestMapping(value = "getBankAccountMgtURL", method = RequestMethod.GET)
    public String getBankAccountMgtURL(Model m) {
       /**
        * 계좌를 등록하는 팝업 URL을 반환합니다.
        * - 권장 사이즈 : width = 1,550px (최소 800px) / height = 680px
        * - 반환되는 URL은 30초 동안만 사용이 가능합니다.
        * - 반환되는 URL에서만 유효한 세션을 포함하고 있습니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#GetBankAccountMgtURL
        */

        try {
            String url = easyFinBankService.getBankAccountMgtURL(CorpNum, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "closeBankAccount", method = RequestMethod.GET)
    public String closeBankAccount(Model m) {
        /**
         * 팝빌에 등록된 계좌의 정액제 해지를 요청합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#CloseBankAccount
         */


        // 은행 기관코드
        String BankCode = "";

        // 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        // 정액제 해지 구분, "일반"
        // 일반(일반해지) – 이용중인 정액제 기간 만료 후 해지
        String CloseType = "일반";

        try {
            Response response = easyFinBankService.closeBankAccount(CorpNum, BankCode, AccountNumber, CloseType, UserID);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "revokeCloseBankAccount", method = RequestMethod.GET)
    public String revokeCloseBankAccount(Model m) {
        /**
         * 신청한 정액제 해지요청을 취소합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#RevokeCloseBankAccount
         */

        // 은행 기관코드
        String BankCode = "";

        // 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        try {
            Response response = easyFinBankService.revokeCloseBankAccount(CorpNum, BankCode, AccountNumber, UserID);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "deleteBankAccount", method = RequestMethod.GET)
    public String deleteBankAccount(Model m) {
       /**
        * 등록된 계좌를 삭제합니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/api/manage#DeleteBankAccount
        */

        // 은행 기관코드
        String BankCode = "";

        // 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        try {
            Response response = easyFinBankService.deleteBankAccount(CorpNum, BankCode, AccountNumber, UserID);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);

            return "exception";
        }


        return "response";
    }

    @RequestMapping(value = "requestJob", method = RequestMethod.GET)
    public String requestJob(Model m) {
       /**
        * 계좌 거래내역 수집을 팝빌에 요청합니다.
        * - 최대 1개월 단위로 수집 요청이 가능하며, 조회일로부터 최대 3개월 이전 내역까지 조회가 가능합니다.
        * - API를 호출하고 반환 받은 작업아이디(JobID)는 수집을 요청한 시점으로부터 1시간 동안만 유효합니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/api/job#RequestJob
        */

        // 은행 기관코드
        String BankCode = "0039";

        // 계좌번호
        String AccountNumber = "2070064401302";

        // 검색 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20250711";

        // 검색 종료일자, 닐짜형식(yyyyMMdd)
        String EDate = "20250731";

        try {
            String jobID = easyFinBankService.requestJob(CorpNum, BankCode, AccountNumber, SDate, EDate, UserID);
            m.addAttribute("Result", jobID);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getJobState", method = RequestMethod.GET)
    public String getJobState(Model m) {
        /**
         * [RequestJob - 수집 요청] API를 호출하고 반환 받은 작업아이디(JobID)를 이용하여 수집 상태를 확인합니다.
         * - 수집 상태(jobState) = 3(완료)이면서, 수집 결과코드(errorCode) = 1(수집성공)인 경우 [Search - 수집 내역 확인] 이 가능합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/job#GetJobState
         */

        // 작업아이디
        String jobID = "022021815000000001";

        try {
            EasyFinBankJobState jobState = easyFinBankService.getJobState(CorpNum, jobID, UserID);
            m.addAttribute("JobState", jobState);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/GetJobState";
    }

    @RequestMapping(value = "listActiveJob", method = RequestMethod.GET)
    public String listActiveJob(Model m) {
        /**
         * [RequestJob - 수집 요청] API를 호출하고 반환 받은 작업아이디(JobID) 목록의 수집 상태를 확인합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/job#ListActiveJob
         */

        try {
            EasyFinBankJobState[] jobState = easyFinBankService.listActiveJob(CorpNum, UserID);
            m.addAttribute("JobStates", jobState);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/ListActiveJob";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /**
         * 금융기관에서 수집된 계좌 거래내역을 확인합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/search#Search
         */

        // 작업아이디
        String jobID = "023011310000000008";

        // 거래유형 ("I" 와 "O" 중 선택, 다중 선택 가능)
        // └ I = 입금 , O = 출금
        // - 미입력 시 전체조회
        String[] TradeType = { "I", "O" };

        // 조회 검색어, "입·출금액" / "거래내역 메모" / "비고" 중 검색하고자 하는 값 입력
        // - 거래내역 메모 = 거래내역 메모저장(SaveMemo API) 함수를 사용하여 저장한 값
        // - 비고 = EasyFinBankSearchDetail의 remark1, remark2, remark3 값
        // - 미입력시 전체조회
        String SearchString = "";

        // 목록 페이지번호
        int Page = 1;

        // 페이지당 표시할 목록 건수
        int PerPage = 10;

        // 목록 정렬 방향, D-내림차순, A-오름차순
        String Order = "D";

        try {
            EasyFinBankSearchResult searchInfo = easyFinBankService.search(CorpNum, jobID, TradeType, SearchString,
                    Page, PerPage, Order, UserID);
            m.addAttribute("SearchResult", searchInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/SearchResult";
    }

    @RequestMapping(value = "summary", method = RequestMethod.GET)
    public String summary(Model m) {
        /**
         * 금융기관에서 수집된 계좌 거래내역의 입금 및 출금 합계정보를 제공합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/api/search#Summary
         */

        // 작업아이디
        String jobID = "022021815000000001";

        // 거래유형 ("I" 와 "O" 중 선택, 다중 선택 가능)
        // └ I = 입금 , O = 출금
        // - 미입력 시 전체조회
        String[] TradeType = { "I", "O" };

        // 조회 검색어, "입·출금액" / "거래내역 메모" / "비고" 중 검색하고자 하는 값 입력
        // - 거래내역 메모 = 거래내역 메모저장(SaveMemo API) 함수를 사용하여 저장한 값
        // - 비고 = EasyFinBankSearchDetail의 remark1, remark2, remark3 값
        // - 미입력시 전체조회
        String SearchString = "";

        try {
            EasyFinBankSummary summaryInfo = easyFinBankService.summary(CorpNum, jobID, TradeType, SearchString, UserID);
            m.addAttribute("SummaryResult", summaryInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/Summary";
    }


    @RequestMapping(value = "saveMemo", method = RequestMethod.GET)
    public String saveMemo(Model m) {
       /**
        * 한 건의 거래 내역에 메모를 저장합니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/api/search#SaveMemo
        */

        // 메모를 저장할 거래내역 아이디
        // └ 거래내역 조회(Seach API) 함수의 반환값인 EasyFinBankSearchDetail 의 tid를 통해 확인 가능
        String TID = "02110251200000000420250711000001";

        // 거래내역 메모
        String Memo = "Memo테스트";

        try {
            Response response = easyFinBankService.saveMemo(CorpNum ,TID, Memo, UserID);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }


    @RequestMapping(value = "getFlatRatePopUpURL", method = RequestMethod.GET)
    public String getFlatRatePopUpURL(Model m) {
       /**
        * 정액제를 신청하는 팝업 URL을 반환합니다.
        * - 권장 사이즈 : width = 800px / height = 700px
        * - 반환되는 URL은 30초 동안만 사용이 가능합니다.
        * - 반환되는 URL에서만 유효한 세션을 포함하고 있습니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/common-api/point#GetFlatRatePopUpURL
        */

        try {
            String url = easyFinBankService.getFlatRatePopUpURL(CorpNum, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }


    @RequestMapping(value = "getFlatRateState", method = RequestMethod.GET)
    public String getFlatRateState(Model m) {
       /**
        * 계좌조회 정액제 서비스 상태를 확인합니다.
        * - https://developers.popbill.com/reference/easyfinbank/java/common-api/point#GetFlatRateState
        */

        // 은행 기관코드
        String BankCode = "";

        // 은행 계좌번호
        String AccountNumber = "";

        try {
            FlatRateState flatRateInfo = easyFinBankService.getFlatRateState(CorpNum, BankCode, AccountNumber, UserID);
            m.addAttribute("State", flatRateInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/GetFlatRateState";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /**
         * 팝빌 계좌조회 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/easyfinbank/java/common-api/point#GetChargeInfo
         */
        try {
            ChargeInfo chrgInfo = easyFinBankService.getChargeInfo(CorpNum, UserID);
            m.addAttribute("ChargeInfo", chrgInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }
}