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
import com.popbill.api.HTTaxinvoiceService;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.hometax.HTTaxinvoice;
import com.popbill.api.hometax.HTTaxinvoiceJobState;
import com.popbill.api.hometax.HTTaxinvoiceSearchResult;
import com.popbill.api.hometax.HTTaxinvoiceSummary;
import com.popbill.api.hometax.HTTaxinvoiceXMLResponse;
import com.popbill.api.hometax.QueryType;

@Controller
@RequestMapping("HTTaxinvoiceService")
public class HTTaxinvoiceServiceController {

    @Autowired
    private HTTaxinvoiceService htTaxinvoiceService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "HTTaxinvoice/index";
    }

    @RequestMapping(value = "requestJob", method = RequestMethod.GET)
    public String requestJob(Model m) {
        /*
         * 홈택스에 신고된 전자세금계산서 매입/매출 내역 수집을 팝빌에 요청합니다. (조회기간 단위 : 최대 3개월) 
         * - https://docs.popbill.com/httaxinvoice/java/api#RequestJob
         */

        // 전자세금계산서 유형, SELL-매출, BUY-매입, TRUSTEE-수탁
        QueryType TIType = QueryType.BUY;

        // 일자유형, W-작성일자, I-발행일자, S-전송일자
        String DType = "S";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20211110";

        // 종료일자, 닐짜형식(yyyyMMdd)
        String EDate = "20211119";

        try {
            String jobID = htTaxinvoiceService.requestJob(testCorpNum, TIType, DType, SDate, EDate);
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
         * 함수 RequestJob(수집 요청)를 통해 반환 받은 작업 아이디의 상태를 확인합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetJobState
         */

        // 수집요청(requestJob)시 반환받은 작업아이디
        String jobID = "021111912000000003";

        try {
            HTTaxinvoiceJobState jobState = htTaxinvoiceService.getJobState(testCorpNum, jobID);
            m.addAttribute("JobState", jobState);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/GetJobState";
    }

    @RequestMapping(value = "listActiveJob", method = RequestMethod.GET)
    public String listActiveJob(Model m) {
        /*
         * 전자세금계산서 매입/매출 내역 수집요청에 대한 상태 목록을 확인합니다. 
         * - 수집 요청 후 1시간이 경과한 수집 요청건은 상태정보가 반환되지 않습니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#ListActiveJob
         */

        try {
            HTTaxinvoiceJobState[] jobStates = htTaxinvoiceService.listActiveJob(testCorpNum);
            m.addAttribute("JobStates", jobStates);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/ListActiveJob";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /*
         * 함수 GetJobState(수집 상태 확인)를 통해 상태 정보가 확인된 작업아이디를 활용하여 수집된 전자세금계산서 매입/매출 내역을 조회합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#Search
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "021111912000000003";

        // 문서형태, N-일반, M-수정
        String[] Type = { "N", "M" };

        // 과세형태, T-과세, N-면세, Z-영세
        String[] TaxType = { "T", "Z", "N" };

        // 영수/청구 R-영수, C-청구, N-없음
        String[] PurposeType = { "R", "C", "N" };

        // 종사업장 유무, 공백-전체조회, 0-종사업장번호 없음, 1-종사업장번호 있음
        String TaxRegIDYN = "";

        // 종사업장 유형, S-공급자, B-공급받는자, T-수탁자
        String TaxRegIDType = "S";

        // 종사업장번호, 다수기재시 콤마(",")로 구분하여 구성 ex) "0001,0002"
        String TaxRegID = "";

        // 페이지번호
        int Page = 1;

        // 페이지당 목록개수
        int PerPage = 10;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        // 조회 검색어, 거래처 사업자번호 또는 거래처명 like 검색
        String searchString = "";

        try {
            HTTaxinvoiceSearchResult searchInfo = htTaxinvoiceService.search(testCorpNum, jobID, Type, TaxType,
                    PurposeType, TaxRegIDYN, TaxRegIDType, TaxRegID, Page, PerPage, Order, testUserID, searchString);
            m.addAttribute("SearchResult", searchInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/SearchResult";
    }

    @RequestMapping(value = "summary", method = RequestMethod.GET)
    public String summary(Model m) {
        /*
         * 함수 GetJobState(수집 상태 확인)를 통해 상태 정보가 확인된 작업아이디를 활용하여 수집된 전자세금계산서 매입/매출 내역의 요약 정보를 조회합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#Summary
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "021111912000000003";

        // 문서형태, N-일반, M-수정
        String[] Type = { "N", "M" };

        // 과세형태, T-과세, N-면세, Z-영세
        String[] TaxType = { "T", "Z", "N" };

        // 영수/청구 R-영수, C-청구, N-없음
        String[] PurposeType = { "R", "C", "N" };

        // 종사업장 유무, 공백-전체조회, 0-종사업장번호 없음, 1-종사업장번호 있음
        String TaxRegIDYN = "";

        // 종사업장 유형, S-공급자, B-공급받는자, T-수탁자
        String TaxRegIDType = "S";

        // 종사업장번호, 다수기재시 콤마(",")로 구분하여 구성 ex) "0001,0002"
        String TaxRegID = "";

        // 조회 검색어, 거래처 사업자번호 또는 거래처명 like 검색
        String searchString = "";

        try {
            HTTaxinvoiceSummary summaryInfo = htTaxinvoiceService.summary(testCorpNum, jobID, Type, TaxType,
                    PurposeType, TaxRegIDYN, TaxRegIDType, TaxRegID, testUserID, searchString);
            m.addAttribute("SummaryResult", summaryInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "HTTaxinvoice/Summary";
    }

    @RequestMapping(value = "getTaxinvoice", method = RequestMethod.GET)
    public String getTaxinvoice(Model m) {
        /*
         * 국세청 승인번호를 통해 수집한 전자세금계산서 1건의 상세정보를 반환합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetTaxinvoice
         */

        // 전자세금계산서 국세청승인번호
        String ntsconfirmNum = "202111104100001142231678";

        try {
            HTTaxinvoice taxinvoiceInfo = htTaxinvoiceService.getTaxinvoice(testCorpNum, ntsconfirmNum);

            m.addAttribute("Taxinvoice", taxinvoiceInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/Taxinvoice";
    }

    @RequestMapping(value = "getXML", method = RequestMethod.GET)
    public String getXML(Model m) {
        /*
         * 국세청 승인번호를 통해 수집한 전자세금계산서 1건의 상세정보를 XML 형태의 문자열로 반환합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetXML
         */

        // 전자세금계산서 국세청승인번호
        String ntsconfirmNum = "202111104100001142231678";

        try {
            HTTaxinvoiceXMLResponse xmlResponse = htTaxinvoiceService.getXML(testCorpNum, ntsconfirmNum);

            m.addAttribute("TaxinvoiceXML", xmlResponse);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/TaxinvoiceXML";
    }

    @RequestMapping(value = "getPopUpURL", method = RequestMethod.GET)
    public String getPopUpURL(Model m) {
        /*
         * 수집된 전자세금계산서 1건의 상세내역을 확인하는 페이지의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetPopUpURL
         */

        // 조회할 전자세금계산서 국세청승인번호
        String NTSConfirmNum = "20211202410002030000196d";

        try {

            String url = htTaxinvoiceService.getPopUpURL(testCorpNum, NTSConfirmNum);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPrintURL", method = RequestMethod.GET)
    public String getPrintURL(Model m) {
        /*
         * 수집된 전자세금계산서 1건의 상세내역을 인쇄하는 페이지의 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetPrintURL
         */

        // 조회할 전자세금계산서 국세청승인번호
        String NTSConfirmNum = "20161202410002030000196d";

        try {

            String url = htTaxinvoiceService.getPrintURL(testCorpNum, NTSConfirmNum);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getCertificatePopUpURL", method = RequestMethod.GET)
    public String getCertificatePopUpURL(Model m) {
        /*
         * 홈택스연동 인증정보를 관리하는 페이지의 팝업 URL을 반환합니다. 
         * - 인증방식에는 부서사용자/공인인증서 인증 방식이 있습니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetCertificatePopUpURL
         */

        try {

            String url = htTaxinvoiceService.getCertificatePopUpURL(testCorpNum);

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
         * 홈택스연동 인증을 위해 팝빌에 등록된 인증서 만료일자를 확인합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetCertificateExpireDate
         */

        try {

            Date expireDate = htTaxinvoiceService.getCertificateExpireDate(testCorpNum);

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
         * - https://docs.popbill.com/httaxinvoice/java/api#CheckCertValidation
         */

        try {

            Response response = htTaxinvoiceService.checkCertValidation(testCorpNum);

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
         * 홈택스연동 인증을 위해 팝빌에 전자세금계산서용 부서사용자 계정을 등록합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#RegistDeptUser
         */

        // 홈택스에서 생성한 전자세금계산서 부서사용자 아이디
        String deptUserID = "userid";

        // 홈택스에서 생성한 전자세금계산서 부서사용자 비밀번호
        String deptUserPWD = "passwd";

        try {

            Response response = htTaxinvoiceService.registDeptUser(testCorpNum, deptUserID, deptUserPWD);

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
         * 홈택스연동 인증을 위해 팝빌에 등록된 전자세금계산서용 부서사용자 계정을 확인합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#CheckDeptUser
         */

        try {

            Response response = htTaxinvoiceService.checkDeptUser(testCorpNum);

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
         * 팝빌에 등록된 전자세금계산서용 부서사용자 계정 정보로 홈택스 로그인 가능 여부를 확인합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#CheckLoginDeptUser
         */

        try {

            Response response = htTaxinvoiceService.checkLoginDeptUser(testCorpNum);

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
         * 팝빌에 등록된 홈택스 전자세금계산서용 부서사용자 계정을 삭제합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#DeleteDeptUser
         */

        try {

            Response response = htTaxinvoiceService.deleteDeptUser(testCorpNum);

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
         * 팝빌 홈택스연동(세금) API 서비스 과금정보를 확인합니다. 
         * - https://docs.popbill.com/httaxinvoice/java/api#GetChargeInfo
         */

        try {

            ChargeInfo chrgInfo = htTaxinvoiceService.getChargeInfo(testCorpNum);
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

            String url = htTaxinvoiceService.getFlatRatePopUpURL(testCorpNum);

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

            FlatRateState flatRateInfo = htTaxinvoiceService.getFlatRateState(testCorpNum);

            m.addAttribute("State", flatRateInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/GetFlatRateState";
    }

}
