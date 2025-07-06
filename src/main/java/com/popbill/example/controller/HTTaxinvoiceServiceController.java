/**
  * 팝빌 홈택스 전자세금계산서 API Java SDK SpringBoot Example
  *
  * SpringBoot 연동 튜토리얼 안내 : https://developers.popbill.com/guide/httaxinvoice/java/getting-started/tutorial?fwn=springboot
  * 연동 기술지원 연락처 : 1600-9854
  * 연동 기술지원 이메일 : code@linkhubcorp.com
  *
  * <테스트 연동개발 준비사항>
  * 1) 홈택스 로그인 인증정보를 등록합니다. (부서사용자등록 / 공동인증서 등록)
  *    - 팝빌로그인 > [홈택스수집] > [환경설정] > [인증 관리] 메뉴
  *    - 홈택스수집 인증 관리 팝업 URL(GetCertificatePopUpURL API) 반환된 URL을 이용하여
  *      홈택스 인증 처리를 합니다.
  */
package com.popbill.example.controller;

import java.util.Date;
import java.util.Locale;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("HTTaxinvoiceService")
public class HTTaxinvoiceServiceController {

    @Autowired
    private HTTaxinvoiceService htTaxinvoiceService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "HTTaxinvoice/index";
    }

    @RequestMapping(value = "requestJob", method = RequestMethod.GET)
    public String requestJob(Model m) {
        /**
         * 홈택스에 신고된 전자세금계산서 매입/매출 내역 수집을 팝빌에 요청합니다.
         * - 최대 3개월 단위로 수집 요청이 가능하며, 수집 기한의 제한은 없습니다.
         * - API를 호출하고 반환 받은 작업아이디(JobID)는 수집을 요청한 시점으로부터 1시간 동안만 유효합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/job#RequestJob
         */

        // 전자세금계산서 유형 (SELL , BUY , TRUSTEE 중 택 1)
        // - SELL = 매출 , BUY = 매입 , TRUSTEE = 수탁
        QueryType TIType = QueryType.BUY;

        // 조회할 일자 유형 ("W" , "I" , "S" 중 택 1)
        // - W = 작성일자 , I = 발행일자 , S = 전송일자
        String DType = "S";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20230102";

        // 종료일자, 닐짜형식(yyyyMMdd)
        String EDate = "20230131";

        try {
            String jobID = htTaxinvoiceService.requestJob(CorpNum, TIType, DType, SDate, EDate);
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
         * - 수집상태(jobState) = 3(완료) 이면서, 수집 결과코드(errorCode) = 1(수집성공)인 경우 [Search - 수집 내역 확인] 이 가능합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/job#GetJobState
         */

        // 수집요청(requestJob)시 반환받은 작업아이디
        String jobID = "";

        try {
            HTTaxinvoiceJobState jobState = htTaxinvoiceService.getJobState(CorpNum, jobID);
            m.addAttribute("JobState", jobState);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/GetJobState";
    }

    @RequestMapping(value = "listActiveJob", method = RequestMethod.GET)
    public String listActiveJob(Model m) {
        /**
         * [RequestJob – 수집 요청] API를 호출하고 반환 받은 작업아이디(JobID) 목록의 수집 상태를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/job#ListActiveJob
         */

        try {
            HTTaxinvoiceJobState[] jobStates = htTaxinvoiceService.listActiveJob(CorpNum);
            m.addAttribute("JobStates", jobStates);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/ListActiveJob";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /**
         * 홈택스에서 수집된 전자세금계산서 매입/매출 내역을 확인합니다.
         * - 38개 항목으로 구성된 내역 확인이 가능합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/search#Search
         */

        // 수집 요청시 발급받은 작업아이디
        String jobID = "";

        // 문서형태 배열 ("N" 와 "M" 중 선택, 다중 선택 가능)
        // └ N = 일반 , M = 수정 , 미입력 시 전체조회
        String[] Type = { "N", "M" };

        // 과세형태 배열 ("T" , "N" , "Z" 중 선택, 다중 선택 가능)
        // └ T = 과세, N = 면세, Z = 영세 , 미입력 시 전체조회
        String[] TaxType = { "T", "Z", "N" };

        // 발행목적 배열 ("R" , "C", "N" 중 선택, 다중 선택 가능)
        // └ R = 영수, C = 청구, N = 없음, 미입력 시 전체조회
        String[] PurposeType = { "R", "C", "N" };

        // 종사업장번호 유무
        // - null = 전체 , 0 = 없음, 1 = 있음
        String TaxRegIDYN = null;

        // 종사업장번호의 주체 ("S" , "B" , "T" 중 택 1)
        // └ S = 공급자 , B = 공급받는자 , T = 수탁자
        // - 미입력시 전체조회
        String TaxRegIDType = "S";

        // 종사업장번호
        // 다수기재시 콤마(",")로 구분하여 구성 ex ) "0001,0002"
        // - 미입력시 전체조회
        String TaxRegID = "";

        // 페이지번호
        int Page = 1;

        // 페이지당 목록 건수
        int PerPage = 10;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        // 거래처 상호 / 사업자번호 (사업자) / 주민등록번호 (개인) / "9999999999999" (외국인) 중 검색하고자 하는 정보 입력
        // - 사업자번호 / 주민등록번호는 하이픈('-')을 제외한 숫자만 입력
        // - 미입력시 전체조회
        String searchString = "";

        try {
            HTTaxinvoiceSearchResult searchInfo = htTaxinvoiceService.search(CorpNum, jobID, Type, TaxType, PurposeType,
                    TaxRegIDYN, TaxRegIDType, TaxRegID, Page, PerPage, Order, UserID, searchString);
            m.addAttribute("SearchResult", searchInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/SearchResult";
    }

    @RequestMapping(value = "summary", method = RequestMethod.GET)
    public String summary(Model m) {
        /**
         * 홈택스에서 수집된 전자세금계산서 매입/매출 내역의 합계정보를 제공합니다.
         * ※ 합계정보 - 수집 건수, 공급가액 합계, 세액 합계, 총계 (공급가액 합계+세액 합계)
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/search#Summary
         */

        // 수집요청(requestJob API) 함수 호출 시 반환받은 작업아이디
        String jobID = "";

        // 문서형태 배열 ("N" 와 "M" 중 선택, 다중 선택 가능)
        // └ N = 일반 , M = 수정 , 미입력 시 전체조회
        String[] Type = { "N", "M" };

        // 과세형태 배열 ("T" , "N" , "Z" 중 선택, 다중 선택 가능)
        // └ T = 과세, N = 면세, Z = 영세 , 미입력 시 전체조회
        String[] TaxType = { "T", "Z", "N" };

        // 발행목적 배열 ("R" , "C", "N" 중 선택, 다중 선택 가능)
        // └ R = 영수, C = 청구, N = 없음, 미입력 시 전체조회
        String[] PurposeType = { "R", "C", "N" };

        // 종사업장번호 유무
        // - null = 전체 , 0 = 없음, 1 = 있음
        String TaxRegIDYN = null;

        // 종사업장번호의 주체 ("S" , "B" , "T" 중 택 1)
        // └ S = 공급자 , B = 공급받는자 , T = 수탁자
        // - 미입력시 전체조회
        String TaxRegIDType = "S";

        // 종사업장번호
        // 다수기재시 콤마(",")로 구분하여 구성 ex ) "0001,0002"
        // - 미입력시 전체조회
        String TaxRegID = "";

        // 거래처 상호 / 사업자번호 (사업자) / 주민등록번호 (개인) / "9999999999999" (외국인) 중 검색하고자 하는 정보 입력
        // - 사업자번호 / 주민등록번호는 하이픈('-')을 제외한 숫자만 입력
        // - 미입력시 전체조회
        String searchString = "";

        try {
            HTTaxinvoiceSummary summaryInfo = htTaxinvoiceService.summary(CorpNum, jobID, Type, TaxType, PurposeType,
                    TaxRegIDYN, TaxRegIDType, TaxRegID, UserID, searchString);
            m.addAttribute("SummaryResult", summaryInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/Summary";
    }

    @RequestMapping(value = "getTaxinvoice", method = RequestMethod.GET)
    public String getTaxinvoice(Model m) {
        /**
         * 홈택스에서 수집된 전자세금계산서 1건의 상세정보를 제공합니다.
         * - 60개 항목과 99개 품목으로 구성된 정보 확인이 가능합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/search#GetTaxinvoice
         */

        // 전자세금계산서 국세청승인번호
        String ntsconfirmNum = "202201058888888800000071";

        try {
            HTTaxinvoice taxinvoiceInfo = htTaxinvoiceService.getTaxinvoice(CorpNum, ntsconfirmNum);
            m.addAttribute("Taxinvoice", taxinvoiceInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/Taxinvoice";
    }

    @RequestMapping(value = "getXML", method = RequestMethod.GET)
    public String getXML(Model m) {
        /**
         * 홈택스에서 수집된 전자세금계산서 1건의 상세정보를 XML 데이터 포맷으로 제공합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/search#GetXML
         */

        // 전자세금계산서 국세청승인번호
        String ntsconfirmNum = "202201058888888800000071";

        try {
            HTTaxinvoiceXMLResponse xmlResponse = htTaxinvoiceService.getXML(CorpNum, ntsconfirmNum);
            m.addAttribute("TaxinvoiceXML", xmlResponse);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/TaxinvoiceXML";
    }

    @RequestMapping(value = "getPopUpURL", method = RequestMethod.GET)
    public String getPopUpURL(Model m) {
        /**
         * 홈택스에서 수집된 전자세금계산서 1건의 팝업 URL을 반환합니다.
         * - 권장 사이즈 : width = 1,000px (최소 950px) / height = 730px
         * - 반환되는 URL은 30초 동안만 사용이 가능합니다.
         * - 반환되는 URL에서만 유효한 세션을 포함하고 있습니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/search#GetPopUpURL
         */

        // 조회할 전자세금계산서 국세청승인번호
        String NTSConfirmNum = "202201058888888800000071";

        try {
            String url = htTaxinvoiceService.getPopUpURL(CorpNum, NTSConfirmNum, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPrintURL", method = RequestMethod.GET)
    public String getPrintURL(Model m) {
        /**
         * 홈택스에서 수집된 전자세금계산서 1건의 인쇄 팝업 URL을 반환합니다.
         * - 권장 사이즈 : width = 930px / height = 765px
         * - 반환되는 URL은 30초 동안만 사용이 가능합니다.
         * - 반환되는 URL에서만 유효한 세션을 포함하고 있습니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/search#GetPrintURL
         */

        // 조회할 전자세금계산서 국세청승인번호
        String NTSConfirmNum = "202201058888888800000071";

        try {
            String url = htTaxinvoiceService.getPrintURL(CorpNum, NTSConfirmNum, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getCertificatePopUpURL", method = RequestMethod.GET)
    public String getCertificatePopUpURL(Model m) {
        /**
         * 홈택스 인증정보를 등록하는 팝업 URL을 반환합니다.
         * - 권장 사이즈 : width = 800px / height = 660px
         * - 반환되는 URL은 30초 동안만 사용이 가능합니다.
         * - 반환되는 URL에서만 유효한 세션을 포함하고 있습니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#GetCertificatePopUpURL
         */

        try {
            String url = htTaxinvoiceService.getCertificatePopUpURL(CorpNum, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getCertificateExpireDate", method = RequestMethod.GET)
    public String getCertificateExpireDate(Model m) {
        /**
         * 팝빌에 등록된 인증서의 만료일자를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#GetCertificateExpireDate
         */

        try {
            Date expireDate = htTaxinvoiceService.getCertificateExpireDate(CorpNum);
            m.addAttribute("Result", expireDate);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "checkCertValidation", method = RequestMethod.GET)
    public String checkCertValidation(Model m) {
        /**
         * 팝빌에 등록된 인증서로 홈택스 로그인 가능 여부를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#CheckCertValidation
         */

        try {
            Response response = htTaxinvoiceService.checkCertValidation(CorpNum);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "registDeptUser", method = RequestMethod.GET)
    public String registDeptUser(Model m) {
        /**
         * 팝빌에 부서사용자를 등록합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#RegistDeptUser
         */

        // 홈택스에서 생성한 전자세금계산서 부서사용자 아이디
        String deptUserID = "userid";

        // 홈택스에서 생성한 전자세금계산서 부서사용자 비밀번호
        String deptUserPWD = "passwd";

        try {
            Response response = htTaxinvoiceService.registDeptUser(CorpNum, deptUserID, deptUserPWD);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "checkDeptUser", method = RequestMethod.GET)
    public String checkDeptUser(Model m) {
        /**
         * 팝빌에 부서사용자 등록 여부를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#CheckDeptUser
         */

        try {
            Response response = htTaxinvoiceService.checkDeptUser(CorpNum);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "checkLoginDeptUser", method = RequestMethod.GET)
    public String checkLoginDeptUser(Model m) {
        /**
         * 팝빌에 등록된 부서사용자로 홈택스 로그인 가능 여부를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#CheckLoginDeptUser
         */

        try {
            Response response = htTaxinvoiceService.checkLoginDeptUser(CorpNum);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "deleteDeptUser", method = RequestMethod.GET)
    public String deleteDeptUser(Model m) {
        /**
         * 팝빌에 등록된 부서사용자 계정을 삭제합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/cert#DeleteDeptUser
         */

        try {
            Response response = htTaxinvoiceService.deleteDeptUser(CorpNum);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /**
         * 팝빌 홈택스수집 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/point#GetChargeInfo
         */

        try {
            ChargeInfo chrgInfo = htTaxinvoiceService.getChargeInfo(CorpNum);
            m.addAttribute("ChargeInfo", chrgInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

    @RequestMapping(value = "getFlatRatePopUpURL", method = RequestMethod.GET)
    public String getFlatRatePopUpURL(Model m) {
        /**
         * 정액제를 신청하는 팝업 URL을 반환합니다.
         * - 권장 사이즈 : width = 800px / height = 700px
         * - 반환되는 URL은 30초 동안만 사용이 가능합니다.
         * - 반환되는 URL에서만 유효한 세션을 포함하고 있습니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/point#GetFlatRatePopUpURL
         */

        try {
            String url = htTaxinvoiceService.getFlatRatePopUpURL(CorpNum, UserID);
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
         * 홈택스수집 정액제 서비스 상태를 확인합니다.
         * - https://developers.popbill.com/reference/httaxinvoice/java/api/point#GetFlatRateState
         */

        try {
            FlatRateState flatRateInfo = htTaxinvoiceService.getFlatRateState(CorpNum);
            m.addAttribute("State", flatRateInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "HTTaxinvoice/GetFlatRateState";
    }

}
