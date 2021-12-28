package com.popbill.example.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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

@Controller
@RequestMapping("EasyFinBankService")
public class EasyFinBankServiceController {

    @Autowired
    private EasyFinBankService easyFinBankService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "EasyFinBank/index";
    }

    @RequestMapping(value = "registBankAccount", method = RequestMethod.GET)
    public String registBankAccount(Model m) {
        /*
         * 계좌조회 서비스를 이용할 계좌를 팝빌에 등록합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#RegistBankAccount
         */

        // 계좌정보 클래스 인스턴스 생성
        EasyFinBankAccountForm bankInfo = new EasyFinBankAccountForm();

        // [필수] 기관코드
        // 산업은행-0002 / 기업은행-0003 / 국민은행-0004 /수협은행-0007 / 농협은행-0011 / 우리은행-0020
        // SC은행-0023 / 대구은행-0031 / 부산은행-0032 / 광주은행-0034 / 제주은행-0035 / 전북은행-0037
        // 경남은행-0039 / 새마을금고-0045 / 신협은행-0048 / 우체국-0071 / KEB하나은행-0081 / 신한은행-0088 /씨티은행-0027
        bankInfo.setBankCode("");

        // [필수] 계좌번호 하이픈('-') 제외
        bankInfo.setAccountNumber("");

        // [필수] 계좌비밀번호
        bankInfo.setAccountPWD("");

        // [필수] 계좌유형, "법인" 또는 "개인" 입력
        bankInfo.setAccountType("");

        // [필수] 예금주 식별정보 (‘-‘ 제외)
        // 계좌유형이 “법인”인 경우 : 사업자번호(10자리)
        // 계좌유형이 “개인”인 경우 : 예금주 생년월일 (6자리-YYMMDD)
        bankInfo.setIdentityNumber("");

        // 계좌 별칭
        bankInfo.setAccountName("");

        // 인터넷뱅킹 아이디 (국민은행 필수)
        bankInfo.setBankID("");

        // 조회전용 계정 아이디 (대구은행, 신협, 신한은행 필수)
        bankInfo.setFastID("");

        // 조회전용 계정 비밀번호 (대구은행, 신협, 신한은행 필수
        bankInfo.setFastPWD("");

        // 결제기간(개월), 1~12 입력가능, 미기재시 기본값(1) 처리
        // - 파트너 과금방식의 경우 입력값에 관계없이 1개월 처리
        bankInfo.setUsePeriod(1);

        // 메모
        bankInfo.setMemo("");

        try {

            Response response = easyFinBankService.registBankAccount(testCorpNum, bankInfo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }


    @RequestMapping(value = "updateBankAccount", method = RequestMethod.GET)
    public String updateBankAccount(Model m) {
        /*
         * 팝빌에 등록된 계좌정보를 수정합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#UpdateBankAccount
         */

        // 계좌정보 클래스 인스턴스 생성
        EasyFinBankAccountForm bankInfo = new EasyFinBankAccountForm();

        // [필수] 기관코드
        // 산업은행-0002 / 기업은행-0003 / 국민은행-0004 /수협은행-0007 / 농협은행-0011 / 우리은행-0020
        // SC은행-0023 / 대구은행-0031 / 부산은행-0032 / 광주은행-0034 / 제주은행-0035 / 전북은행-0037
        // 경남은행-0039 / 새마을금고-0045 / 신협은행-0048 / 우체국-0071 / KEB하나은행-0081 / 신한은행-0088 /씨티은행-0027
        bankInfo.setBankCode("");

        // [필수] 계좌번호 하이픈('-') 제외
        bankInfo.setAccountNumber("");

        // [필수] 계좌비밀번호
        bankInfo.setAccountPWD("");

        // 계좌 별칭
        bankInfo.setAccountName("");

        // 인터넷뱅킹 아이디 (국민은행 필수)
        bankInfo.setBankID("");

        // 조회전용 계정 아이디 (대구은행, 신협, 신한은행 필수)
        bankInfo.setFastID("");

        // 조회전용 계정 비밀번호 (대구은행, 신협, 신한은행 필수
        bankInfo.setFastPWD("");

        // 메모
        bankInfo.setMemo("");

        try {

            Response response = easyFinBankService.updateBankAccount(testCorpNum, bankInfo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getBankAccountInfo", method = RequestMethod.GET)
    public String getBankAccountInfo(Model m) {
       /*
        * 팝빌에 등록된 계좌 정보를 확인합니다.
        * - https://docs.popbill.com/easyfinbank/java/api#GetBankAccountInfo
        */

        // [필수] 기관코드
        // 산업은행-0002 / 기업은행-0003 / 국민은행-0004 /수협은행-0007 / 농협은행-0011 / 우리은행-0020
        // SC은행-0023 / 대구은행-0031 / 부산은행-0032 / 광주은행-0034 / 제주은행-0035 / 전북은행-0037
        // 경남은행-0039 / 새마을금고-0045 / 신협은행-0048 / 우체국-0071 / KEB하나은행-0081 / 신한은행-0088 /씨티은행-0027
        String BankCode = "0003";

        // [필수] 계좌번호 하이픈('-') 제외
        String AccountNumber = "47906631701015";

        try {

            EasyFinBankAccount bankAccountInfo = easyFinBankService.getBankAccountInfo(testCorpNum, BankCode, AccountNumber);

            m.addAttribute("Account", bankAccountInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/GetBankAccount";
    }

   @RequestMapping(value = "listBankAccount", method = RequestMethod.GET)
    public String listBankAccount(Model m) {
       /*
        * 팝빌에 등록된 은행계좌 목록을 반환한다.
        * - https://docs.popbill.com/easyfinbank/java/api#ListBankAccount
        */
        try {

            EasyFinBankAccount[] bankList = easyFinBankService.listBankAccount(testCorpNum);
            m.addAttribute("BankAccountList", bankList);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/ListBankAccount";
    }

   @RequestMapping(value = "getBankAccountMgtURL", method = RequestMethod.GET)
    public String getBankAccountMgtURL(Model m) {
       /*
        * 계좌 등록, 수정 및 삭제할 수 있는 계좌 관리 팝업 URL을 반환합니다.
        * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
        * - https://docs.popbill.com/easyfinbank/java/api#GetBankAccountMgtURL
        */

        try {
            String url = easyFinBankService.getBankAccountMgtURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "closeBankAccount", method = RequestMethod.GET)
    public String closeBankAccount(Model m) {
        /*
         * 계좌의 정액제 해지를 요청합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#CloseBankAccount
         */


        // [필수] 기관코드
        // 산업은행-0002 / 기업은행-0003 / 국민은행-0004 /수협은행-0007 / 농협은행-0011 / 우리은행-0020
        // SC은행-0023 / 대구은행-0031 / 부산은행-0032 / 광주은행-0034 / 제주은행-0035 / 전북은행-0037
        // 경남은행-0039 / 새마을금고-0045 / 신협은행-0048 / 우체국-0071 / KEB하나은행-0081 / 신한은행-0088 /씨티은행-0027
        String BankCode = "";

        // [필수] 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        // [필수] 해지유형, “일반”, “중도” 중 선택 기재
        // 일반해지 – 이용중인 정액제 사용기간까지 이용후 정지
        // 중도해지 – 요청일 기준으로 정지, 정액제 잔여기간은 일할로 계산되어 포인트 환불 (무료 이용기간 중 중도해지 시 전액 환불)
        String CloseType = "중도";

        try {

            Response response = easyFinBankService.closeBankAccount(testCorpNum, BankCode, AccountNumber, CloseType);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "revokeCloseBankAccount", method = RequestMethod.GET)
    public String revokeCloseBankAccount(Model m) {
        /*
         * 신청한 정액제 해지요청을 취소합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#RevokeCloseBankAccount
         */

        // [필수] 기관코드
        // 산업은행-0002 / 기업은행-0003 / 국민은행-0004 /수협은행-0007 / 농협은행-0011 / 우리은행-0020
        // SC은행-0023 / 대구은행-0031 / 부산은행-0032 / 광주은행-0034 / 제주은행-0035 / 전북은행-0037
        // 경남은행-0039 / 새마을금고-0045 / 신협은행-0048 / 우체국-0071 / KEB하나은행-0081 / 신한은행-0088 /씨티은행-0027
        String BankCode = "";

        // [필수] 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        try {

            Response response = easyFinBankService.revokeCloseBankAccount(testCorpNum, BankCode, AccountNumber);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "deleteBankAccount", method = RequestMethod.GET)
    public String deleteBankAccount(Model m) {
       /*
        * 등록된 계좌를 삭제합니다.
        * - 정액제가 아닌 종량제 이용 시에만 등록된 계좌를 삭제할 수 있습니다.
        * - https://docs.popbill.com/easyfinbank/java/api#DeleteBankAccount
        */

        // [필수] 기관코드
        // 산업은행-0002 / 기업은행-0003 / 국민은행-0004 /수협은행-0007 / 농협은행-0011 / 우리은행-0020
        // SC은행-0023 / 대구은행-0031 / 부산은행-0032 / 광주은행-0034 / 제주은행-0035 / 전북은행-0037
        // 경남은행-0039 / 새마을금고-0045 / 신협은행-0048 / 우체국-0071 / KEB하나은행-0081 / 신한은행-0088 /씨티은행-0027
        String BankCode = "";

        // [필수] 계좌번호 하이픈('-') 제외
        String AccountNumber = "";

        try {
            Response response = easyFinBankService.deleteBankAccount(testCorpNum, BankCode, AccountNumber);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);

            return "exception";
        }


        return "response";
    }

    @RequestMapping(value = "requestJob", method = RequestMethod.GET)
    public String requestJob(Model m) {
       /*
        * 계좌 거래내역을 확인하기 위해 팝빌에 수집요청을 합니다. (조회기간 단위 : 최대 1개월)
        * - 조회일로부터 최대 3개월 이전 내역까지 조회할 수 있습니다.
        * - https://docs.popbill.com/easyfinbank/java/api#RequestJob
        */

        // 기관코드
        String BankCode = "0003";

        // 계좌번호
        String AccountNumber = "47906631701015";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20211010";

        // 종료일자, 닐짜형식(yyyyMMdd)
        String EDate = "20211119";

        try {

            String jobID = easyFinBankService.requestJob(testCorpNum, BankCode, AccountNumber, SDate, EDate);
            m.addAttribute("Result", jobID);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getJobState", method = RequestMethod.GET)
    public String getJobState(Model m) {
        /*
         * RequestJob(수집 요청)를 통해 반환 받은 작업아이디의 상태를 확인합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#GetJobState
         */

        // 수집요청(requestJob)시 반환받은 작업아이디
        String jobID = "021111917000000017";

        try {
            EasyFinBankJobState jobState = easyFinBankService.getJobState(testCorpNum, jobID);
            m.addAttribute("JobState", jobState);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/GetJobState";
    }

    @RequestMapping(value = "listActiveJob", method = RequestMethod.GET)
    public String listActiveJob(Model m) {
        /*
         * RequestJob(수집 요청)를 통해 반환 받은 작업아이디의 목록을 확인합니다.
         * - 수집 요청 후 1시간이 경과한 수집 요청건은 상태정보가 반환되지 않습니다.
         * - https://docs.popbill.com/easyfinbank/java/api#ListActiveJob
         */

        try {
            EasyFinBankJobState[] jobState = easyFinBankService.listActiveJob(testCorpNum);
            m.addAttribute("JobStates", jobState);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/ListActiveJob";
    }


    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /*
         * GetJobState(수집 상태 확인)를 통해 상태 정보가 확인된 작업아이디를 활용하여 계좌 거래 내역을 조회합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#Search
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "021112219000000029";

        // 거래유형, I-입금, O-출금
        String[] TradeType = {"I", "O"};

        // 페이지번호
        int Page = 1;

        // 페이지당 목록개수
        int PerPage = 10;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        // 조회 검색어, 입금/출금액, 메모, 적요 like 검색
        String SearchString = "";

        try {
            EasyFinBankSearchResult searchInfo = easyFinBankService.search(testCorpNum,
                    jobID, TradeType, SearchString, Page, PerPage, Order, testUserID);
            m.addAttribute("SearchResult", searchInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/SearchResult";
    }


    @RequestMapping(value = "summary", method = RequestMethod.GET)
    public String summary(Model m) {
        /*
         * GetJobState(수집 상태 확인)를 통해 상태 정보가 확인된 작업아이디를 활용하여 계좌 거래내역의 요약 정보를 조회합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#Summary
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "021112219000000029";

        // 거래유형, I-입금, O-출금
        String[] TradeType = {"I", "O"};

        // 조회 검색어, 입금/출금액, 메모, 적요 like 검색
        String SearchString = "";

        try {

            EasyFinBankSummary summaryInfo = easyFinBankService.summary(testCorpNum,
                    jobID, TradeType, SearchString, testUserID);
            m.addAttribute("SummaryResult", summaryInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/Summary";
    }


    @RequestMapping(value = "saveMemo", method = RequestMethod.GET)
    public String saveMemo(Model m) {
       /*
        * 한 건의 거래 내역에 메모를 저장합니다.
        * - https://docs.popbill.com/easyfinbank/java/api#SaveMemo
        */

        // 거래내역 아이디, SeachAPI 응답항목 중 tid
        String TID = "02111121500000000120211119000001";

        // 메모
        String Memo = "211122-TEST";

        try {

            Response response = easyFinBankService.saveMemo(testCorpNum ,TID, Memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }


    @RequestMapping(value = "getFlatRatePopUpURL", method = RequestMethod.GET)
    public String getFlatRatePopUpURL(Model m) {
       /*
        * 계좌조회 정액제 서비스 신청 페이지의 팝업 URL을 반환합니다.
        * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
        * - https://docs.popbill.com/easyfinbank/java/api#GetFlatRatePopUpURL
        */
        try {
            String url = easyFinBankService.getFlatRatePopUpURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }


    @RequestMapping(value = "getFlatRateState", method = RequestMethod.GET)
    public String getFlatRateState(Model m) {
       /*
        * 계좌조회 정액제 서비스 상태를 확인합니다.
        * - https://docs.popbill.com/easyfinbank/java/api#GetFlatRateState
        */

        // 기관코드
        String BankCode = "0003";

        // 계좌번호
        String AccountNumber = "47906631701015";

        try {

            FlatRateState flatRateInfo = easyFinBankService.getFlatRateState(testCorpNum, BankCode, AccountNumber);

            m.addAttribute("State", flatRateInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "EasyFinBank/GetFlatRateState";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /*
         * 팝빌 계좌조회 API 서비스 과금정보를 확인합니다.
         * - https://docs.popbill.com/easyfinbank/java/api#GetChargeInfo
         */
        try {

            ChargeInfo chrgInfo = easyFinBankService.getChargeInfo(testCorpNum);
            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }
}
