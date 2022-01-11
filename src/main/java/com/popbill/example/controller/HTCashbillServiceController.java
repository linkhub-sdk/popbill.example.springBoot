package com.popbill.example.controller;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.ChargeInfo;
import com.popbill.api.FlatRateState;
import com.popbill.api.HTCashbillService;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.hometax.HTCashbillJobState;
import com.popbill.api.hometax.HTCashbillSearchResult;
import com.popbill.api.hometax.HTCashbillSummary;
import com.popbill.api.hometax.QueryType;

@Controller
@RequestMapping("HTCashbillService")
public class HTCashbillServiceController {

    @Autowired
    private HTCashbillService htCashbillService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "HTCashbill/index";
    }

    @RequestMapping(value = "requestJob", method = RequestMethod.GET)
    public String requestJob(Model m) {
        /*
         * 홈택스에 신고된 현금영수증 매입/매출 내역 수집을 팝빌에 요청합니다. (조회기간 단위 : 최대 3개월)
         * - https://docs.popbill.com/htcashbill/java/api#RequestJob
         */

        // 현금영수증 유형, SELL-매출, BUY-매입
        QueryType TIType = QueryType.SELL;

        // 시작일자, 표시형식(yyyyMMdd)
        String SDate = "20211220";

        // 종료일자, 표시형식(yyyyMMdd)
        String EDate = "20220110";

        try {
            String jobID = htCashbillService.requestJob(testCorpNum, TIType, SDate, EDate, testUserID);
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
         * 수집 요청(RequestJob API) 함수를 통해 반환 받은 작업 아이디의 상태를 확인합니다.
         * - 수집 결과 조회(Search API) 함수 또는 수집 결과 요약 정보 조회(Summary API) 함수를 사용하기 전에
         *   수집 작업의 진행 상태, 수집 작업의 성공 여부를 확인해야 합니다.
         * - 작업 상태(jobState) = 3(완료)이고 수집 결과 코드(errorCode) = 1(수집성공)이면
         *   수집 결과 조회(Search) 또는 수집 결과 요약 정보 조회(Summary) 를 해야합니다.
         * - 작업 상태(jobState)가 3(완료)이지만 수집 결과 코드(errorCode)가 1(수집성공)이 아닌 경우에는
         *   오류메시지(errorReason)로 수집 실패에 대한 원인을 파악할 수 있습니다.
         * - https://docs.popbill.com/htcashbill/java/api#GetJobState
         */

        // 수집요청(requestJob)시 반환받은 작업아이디
        String jobID = "022011111000000004";

        try {
            HTCashbillJobState jobState = htCashbillService.getJobState(testCorpNum, jobID);
            m.addAttribute("JobState", jobState);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTCashbill/GetJobState";
    }

    @RequestMapping(value = "listActiveJob", method = RequestMethod.GET)
    public String listActiveJob(Model m) {
        /*
         * 현금영수증 매입/매출 내역 수집요청에 대한 상태 목록을 확인합니다.
         * - 수집 요청 후 1시간이 경과한 수집 요청건은 상태정보가 반환되지 않습니다.
         * - https://docs.popbill.com/htcashbill/java/api#ListActiveJob
         */

        try {
            HTCashbillJobState[] jobStates = htCashbillService.listActiveJob(testCorpNum);
            m.addAttribute("JobStates", jobStates);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTCashbill/ListActiveJob";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /*
         * 수집 상태 확인(GetJobState API) 함수를 통해 상태 정보 확인된 작업아이디를 활용하여 현금영수증 매입/매출 내역을 조회합니다.
         * - https://docs.popbill.com/htcashbill/java/api#Search
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "022011111000000004";

        // 거래구분 배열 ("P" 와 "C" 중 선택, 다중 선택 가능)
        // └ P = 소득공제용 , C = 지출증빙용 , 미입력 시 전체조회
        String[] TradeUsage = { "P", "C" };

        // 문서형태 배열 ("N" 와 "C" 중 선택, 다중 선택 가능)
        // └ N = 일반 현금영수증 , C = 취소현금영수증 , 미입력 시 전체조회
        String[] TradeType = { "N", "C" };

        // 페이지번호
        int Page = 1;

        // 페이지당 목록개수
        int PerPage = 10;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        try {
            HTCashbillSearchResult searchInfo = htCashbillService.search(testCorpNum, jobID, TradeUsage, TradeType,
                    Page, PerPage, Order);
            m.addAttribute("SearchResult", searchInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTCashbill/SearchResult";
    }

    @RequestMapping(value = "summary", method = RequestMethod.GET)
    public String summary(Model m) {
        /*
         * 수집 상태 확인(GetJobState API) 함수를 통해 상태 정보가 확인된 작업아이디를 활용하여 수집된 현금영수증 매입/매출 내역의 요약 정보를 조회합니다.
         * - https://docs.popbill.com/htcashbill/java/api#Summary
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "022011111000000004";

        // 거래구분 배열 ("P" 와 "C" 중 선택, 다중 선택 가능)
        // └ P = 소득공제용 , C = 지출증빙용 , 미입력 시 전체조회
        String[] TradeUsage = { "P", "C" };

        // 문서형태 배열 ("N" 와 "C" 중 선택, 다중 선택 가능)
        // └ N = 일반 현금영수증 , C = 취소현금영수증 , 미입력 시 전체조회
        String[] TradeType = { "N", "C" };

        try {
            HTCashbillSummary summaryInfo = htCashbillService.summary(testCorpNum, jobID, TradeUsage, TradeType);
            m.addAttribute("SummaryResult", summaryInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "HTCashbill/Summary";
    }

    @RequestMapping(value = "getCertificatePopUpURL", method = RequestMethod.GET)
    public String getCertificatePopUpURL(Model m) {
        /*
         * 홈택스연동 인증정보를 관리하는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/htcashbill/java/api#GetCertificatePopUpURL
         */

        try {

            String url = htCashbillService.getCertificatePopUpURL(testCorpNum);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getCertificateExpireDate", method = RequestMethod.GET)
    public String getCertificateExpireDate(Model m) {
        /*
         * 팝빌에 등록된 인증서 만료일자를 확인합니다.
         * - https://docs.popbill.com/htcashbill/java/api#GetCertificateExpireDate
         */
        try {

            Date expireDate = htCashbillService.getCertificateExpireDate(testCorpNum);

            m.addAttribute("Result", expireDate);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "checkCertValidation", method = RequestMethod.GET)
    public String checkCertValidation(Model m) {
        /*
         * 팝빌에 등록된 인증서로 홈택스 로그인 가능 여부를 확인합니다.
         * - https://docs.popbill.com/htcashbill/java/api#CheckCertValidation
         */
        try {

            Response response = htCashbillService.checkCertValidation(testCorpNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "registDeptUser", method = RequestMethod.GET)
    public String registDeptUser(Model m) {
        /*
         * 홈택스연동 인증을 위해 팝빌에 현금영수증 자료조회 부서사용자 계정을 등록합니다.
         * - https://docs.popbill.com/htcashbill/java/api#RegistDeptUser
         */

        // 홈택스에서 생성한 현금영수증 부서사용자 아이디
        String deptUserID = "userid";

        // 홈택스에서 생성한 현금영수증 부서사용자 비밀번호
        String deptUserPWD = "passwd";

        try {

            Response response = htCashbillService.registDeptUser(testCorpNum, deptUserID, deptUserPWD);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "checkDeptUser", method = RequestMethod.GET)
    public String checkDeptUser(Model m) {
        /*
         * 홈택스연동 인증을 위해 팝빌에 등록된 현금영수증 자료조회 부서사용자 계정을 확인합니다.
         * - https://docs.popbill.com/htcashbill/java/api#CheckDeptUser
         */
        try {

            Response response = htCashbillService.checkDeptUser(testCorpNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "checkLoginDeptUser", method = RequestMethod.GET)
    public String checkLoginDeptUser(Model m) {
        /*
         * 팝빌에 등록된 현금영수증 자료조회 부서사용자 계정 정보로 홈택스 로그인 가능 여부를 확인합니다.
         * - https://docs.popbill.com/htcashbill/java/api#CheckLoginDeptUser
         */
        try {

            Response response = htCashbillService.checkLoginDeptUser(testCorpNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "deleteDeptUser", method = RequestMethod.GET)
    public String deleteDeptUser(Model m) {
        /*
         * 팝빌에 등록된 홈택스 현금영수증 자료조회 부서사용자 계정을 삭제합니다.
         * - https://docs.popbill.com/htcashbill/java/api#DeleteDeptUser
         */
        try {

            Response response = htCashbillService.deleteDeptUser(testCorpNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /*
         * 팝빌 홈택스연동(현금영수증) API 서비스 과금정보를 확인합니다.
         * - https://docs.popbill.com/httaxinvoice/java/api#GetChargeInfo
         */

        try {

            ChargeInfo chrgInfo = htCashbillService.getChargeInfo(testCorpNum);
            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

    @RequestMapping(value = "getFlatRatePopUpURL", method = RequestMethod.GET)
    public String getFlatRatePopUpURL(Model m) {
        /*
         * 홈택스연동 정액제 서비스 신청 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/httaxinvoice/java/api#GetFlatRatePopUpURL
         */

        try {

            String url = htCashbillService.getFlatRatePopUpURL(testCorpNum);

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
         * 홈택스연동 정액제 서비스 상태를 확인합니다.
         * - https://docs.popbill.com/httaxinvoice/java/api#GetFlatRateState
         */

        try {

            FlatRateState flatRateInfo = htCashbillService.getFlatRateState(testCorpNum);

            m.addAttribute("State", flatRateInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/GetFlatRateState";
    }
}
