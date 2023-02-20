package com.popbill.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.ContactInfo;
import com.popbill.api.CorpInfo;
import com.popbill.api.JoinForm;
import com.popbill.api.PaymentForm;
import com.popbill.api.PaymentHistory;
import com.popbill.api.PaymentHistoryResult;
import com.popbill.api.PaymentResponse;
import com.popbill.api.PopbillException;
import com.popbill.api.RefundForm;
import com.popbill.api.RefundHistoryResult;
import com.popbill.api.Response;
import com.popbill.api.TaxinvoiceService;
import com.popbill.api.UseHistoryResult;

@Controller
@RequestMapping("BaseService")
public class BaseServiceServiceController {

    @Autowired
    private TaxinvoiceService taxinvoiceService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    // 링크아이디
    private String testLinkID = "TESTER";

    @RequestMapping(value = "checkIsMember", method = RequestMethod.GET)
    public String checkIsMember(Model m) throws PopbillException {
        /*
         * 사업자번호를 조회하여 연동회원 가입여부를 확인합니다.
         * - LinkID는 연동신청 시 팝빌에서 발급받은 링크아이디 값입니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#CheckIsMember
         */

        // 조회할 사업자번호, '-' 제외 10자리
        String corpNum = "1234567890";

        try {
            Response response = taxinvoiceService.checkIsMember(corpNum, testLinkID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getBalance", method = RequestMethod.GET)
    public String getBalance(Model m) throws PopbillException {
        /*
         * 연동회원의 잔여포인트를 확인합니다.
         * - 과금방식이 파트너과금인 경우 파트너 잔여포인트 확인(GetPartnerBalance API) 함수를 통해 확인하시기 바랍니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetBalance
         */

        try {
            double remainPoint = taxinvoiceService.getBalance(testCorpNum);

            m.addAttribute("Result", remainPoint);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPartnerBalance", method = RequestMethod.GET)
    public String getPartnerBalance(Model m) throws PopbillException {
        /*
         * 파트너의 잔여포인트를 확인합니다.
         * - 과금방식이 연동과금인 경우 연동회원 잔여포인트 조회(GetBalance API) 함수를 이용하시기 바랍니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetPartnerBalance
         */

        try {
            double remainPoint = taxinvoiceService.getPartnerBalance(testCorpNum);

            m.addAttribute("Result", remainPoint);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getUseHistory", method = RequestMethod.GET)
    public String getUseHistory(Model m) throws PopbillException {
        /*
         * 연동회원의 포인트 사용내역을 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetUseHistory
         */

    	// 조회 기간의 시작일자 (형식 : yyyyMMdd)
        String SDate = "20230101";
        
        // 조회 기간의 종료일자 (형식 : yyyyMMdd)
        String EDate = "20230131";
        
        // 목록 페이지번호 (기본값 1)
        Integer Page = 1;
        
        // 페이지당 표시할 목록 개수 (기본값 500, 최대 1,000)
        Integer PerPage = 100;
        
        // 거래일자를 기준으로 하는 목록 정렬 방향 : "D" / "A" 중 택 1
        // └ "D" : 내림차순
        // └ "A" : 오름차순
        // ※ 미입력시 기본값 "D" 처리
        String Order = "D";

        try {
            UseHistoryResult useHistoryResult = taxinvoiceService.getUseHistory(testCorpNum, SDate, EDate, Page, PerPage, Order);

            m.addAttribute("UseHistoryResult", useHistoryResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "useHistoryResult";
    }

    @RequestMapping(value = "getPaymentHistory", method = RequestMethod.GET)
    public String getPaymentHistory(Model m) throws PopbillException {
        /*
         * 연동회원의 포인트 결제내역을 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetPaymentHistory
         */

    	// 조회 기간의 시작일자 (형식 : yyyyMMdd)
        String SDate = "20230101";
        
        // 조회 기간의 종료일자 (형식 : yyyyMMdd)
        String EDate = "20230131";
        
        // 목록 페이지번호 (기본값 1)
        Integer Page = 1;
        
        // 	페이지당 표시할 목록 개수 (기본값 500, 최대 1,000)
        Integer PerPage = 100;

        try {
            PaymentHistoryResult paymentHistoryResult = taxinvoiceService.getPaymentHistory(testCorpNum, SDate, EDate, Page, PerPage);

            m.addAttribute("PaymentHistoryResult", paymentHistoryResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "paymentHistoryResult";
    }

    @RequestMapping(value = "getRefundHistory", method = RequestMethod.GET)
    public String getRefundHistory(Model m) throws PopbillException {
        /*
         * 연동회원의 포인트 환불신청내역을 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetRefundHistory
         */

    	// 목록 페이지번호 (기본값 1)
        Integer Page = 1;
        
        // 페이지당 표시할 목록 개수 (기본값 500, 최대 1,000)
        Integer PerPage = 100;

        try {
            RefundHistoryResult refundHistoryResult = taxinvoiceService.getRefundHistory(testCorpNum, Page, PerPage);

            m.addAttribute("RefundHistoryResult", refundHistoryResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "refundHistoryResult";
    }

    @RequestMapping(value = "refund", method = RequestMethod.GET)
    public String refund(Model m) throws PopbillException {
        /*
         * 연동회원 포인트를 환불 신청합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#Refund
         */

        RefundForm refundForm = new RefundForm();

        // 담당자명
        refundForm.setContactName("담당자명");
        
        // 담당자 연락처
        refundForm.setTel("01077777777");
        
        // 환불 신청 포인트
        refundForm.setRequestPoint("10");
        
        // 은행명
        refundForm.setAccountBank("국민");
        
        // 계좌번호
        refundForm.setAccountNum("123123123-123");
        
        // 예금주명
        refundForm.setAccountName("예금주명");
        
        // 환불사유
        refundForm.setReason("환불사유");

        try {
            Response response = taxinvoiceService.refund(testCorpNum, refundForm);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "paymentRequest", method = RequestMethod.GET)
    public String paymentRequest(Model m) throws PopbillException {
        /*
         * 연동회원 포인트 충전을 위해 무통장입금을 신청합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#PaymentRequest
         */

        PaymentForm paymentForm = new PaymentForm();

        // 담당자명
        paymentForm.setSettlerName("담당자명");
        
        // 담당자 이메일
        paymentForm.setSettlerEmail("test@test.com");
        
        // 담당자 휴대폰
        // └ 무통장 입금 승인 알림톡이 전송될 번호
        paymentForm.setNotifyHP("01012341234");
        
        // 입금자명
        paymentForm.setPaymentName("입금자명");
        
        // 결제금액
        paymentForm.setSettleCost("11000");

        try {
            PaymentResponse paymentResponse = taxinvoiceService.paymentRequest(testCorpNum, paymentForm);

            m.addAttribute("PaymentResponse", paymentResponse);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "paymentResponse";
    }

    @RequestMapping(value = "getSettleResult", method = RequestMethod.GET)
    public String getSettleResult(Model m) throws PopbillException {
        /*
         * 연동회원 포인트 무통장 입금신청내역 1건을 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetSettleResult
         */

    	// 정산코드
        String settleCode = "202301160000000010";

        try {
            PaymentHistory paymentHistory = taxinvoiceService.getSettleResult(testCorpNum, settleCode);

            m.addAttribute("PaymentHistory", paymentHistory);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "paymentHistory";
    }

    @RequestMapping(value = "getPartnerURL", method = RequestMethod.GET)
    public String getPartnerURL(Model m) throws PopbillException {
        /*
         * 파트너 포인트 충전을 위한 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetPartnerURL
         */

        // CHRG : 포인트 충전
        String TOGO = "CHRG";

        try {

            String url = taxinvoiceService.getPartnerURL(testCorpNum, TOGO);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getAccessURL", method = RequestMethod.GET)
    public String getAccessURL(Model m) throws PopbillException {
        /*
         * 팝빌 사이트에 로그인 상태로 접근할 수 있는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#GetAccessURL
         */
        try {

            String url = taxinvoiceService.getAccessURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getChargeURL", method = RequestMethod.GET)
    public String getChargeURL(Model m) throws PopbillException {
        /*
         * 연동회원 포인트 충전을 위한 페이지의 팝업 URL을 반환합니다.
         * 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetChargeURL
         */
        try {

            String url = taxinvoiceService.getChargeURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPaymentURL", method = RequestMethod.GET)
    public String getPaymentURL(Model m) throws PopbillException {
        /*
         * 연동회원 포인트 결제내역 확인을 위한 페이지의 팝업 URL을 반환합니다.
         * 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetPaymentURL
         */
        try {

            String url = taxinvoiceService.getPaymentURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getUseHistoryURL", method = RequestMethod.GET)
    public String getUseHistoryURL(Model m) throws PopbillException {
        /*
         * 연동회원 포인트 사용내역 확인을 위한 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/point#GetUseHistoryURL
         */
        try {

            String url = taxinvoiceService.getUseHistoryURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "joinMember", method = RequestMethod.GET)
    public String joinMember(Model m) throws PopbillException {
        /*
         * 사용자를 연동회원으로 가입처리합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#JoinMember
         */

        JoinForm joinInfo = new JoinForm();

        // 아이디, 6자 이상 50자 미만
        joinInfo.setID("testkorea0328");

        // 팝빌회원 비밀번호 (8자 이상 20자 이하) 영문, 숫자, 특수문자 조합
        joinInfo.setPassword("password123!@#");

        // 연동신청 시 팝빌에서 발급받은 링크아이디
        joinInfo.setLinkID(testLinkID);

        // 사업자번호 (하이픈 '-' 제외 10 자리)
        joinInfo.setCorpNum("1234567890");

        // 대표자 성명, 최대 100자
        joinInfo.setCEOName("대표자 성명");

        // 회사명, 최대 200자
        joinInfo.setCorpName("회사명");

        // 사업장 주소, 최대 300자
        joinInfo.setAddr("주소");

        // 업태, 최대 100자
        joinInfo.setBizType("업태");

        // 종목, 최대 100자
        joinInfo.setBizClass("종목");

        // 담당자 성명, 최대 100자
        joinInfo.setContactName("담당자 성명");

        // 담당자 이메일, 최대 100자
        joinInfo.setContactEmail("test@test.com");

        // 담당자 연락처, 최대 20자
        joinInfo.setContactTEL("02-999-9999");

        try {

            Response response = taxinvoiceService.joinMember(joinInfo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getContactInfo", method = RequestMethod.GET)
    public String getContactInfo(Model m) throws PopbillException {
        /*
         * 연동회원 사업자번호에 등록된 담당자(팝빌 로그인 계정) 정보를 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#GetContactInfo
         */

        // 담당자 아이디
        String contactID = "testkorea";

        try {
            ContactInfo response = taxinvoiceService.getContactInfo(testCorpNum, contactID);

            m.addAttribute("ContactInfo", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "getContactInfo";
    }

    @RequestMapping(value = "listContact", method = RequestMethod.GET)
    public String listContact(Model m) throws PopbillException {
        /*
         * 연동회원 사업자번호에 등록된 담당자(팝빌 로그인 계정) 목록을 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#ListContact
         */

        try {
            ContactInfo[] response = taxinvoiceService.listContact(testCorpNum);

            m.addAttribute("ContactInfos", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "listContact";
    }

    @RequestMapping(value = "updateContact", method = RequestMethod.GET)
    public String updateContact(Model m) throws PopbillException {
        /*
         * 연동회원 사업자번호에 등록된 담당자(팝빌 로그인 계정) 정보를 수정합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#UpdateContact
         */

        ContactInfo contactInfo = new ContactInfo();

        // 담당자 아이디, 6자 이상 50자 이하
        contactInfo.setId("testid");

        // 담당자 성명, 최대 100자
        contactInfo.setPersonName("담당자 수정 테스트");

        // 담당자 연락처, 최대 20자
        contactInfo.setTel("070-1234-1234");

        // 담당자 이메일, 최대 100자
        contactInfo.setEmail("test1234@test.com");

        // 담당자 조회권한, 1 - 개인권한 / 2 - 읽기권한 / 3 - 회사권한
        contactInfo.setSearchRole(3);

        try {

            Response response = taxinvoiceService.updateContact(testCorpNum, contactInfo, testUserID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "registContact", method = RequestMethod.GET)
    public String registContact(Model m) throws PopbillException {
        /*
         * 연동회원 사업자번호에 담당자(팝빌 로그인 계정)를 추가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#RegistContact
         */

        ContactInfo contactInfo = new ContactInfo();

        // 담당자 아이디, 6자 이상 50자 이하
        contactInfo.setId("testid");

        // 담당자 비밀번호 (8자 이상 20자 이하) 영문, 숫자, 특수문자 조합
        contactInfo.setPassword("password123!@#");

        // 담당자 성명, 최대 100자
        contactInfo.setPersonName("담당자 수정 테스트");

        // 담당자 연락처, 최대 20자
        contactInfo.setTel("070-1234-1234");

        // 담당자 이메일, 최대 100자
        contactInfo.setEmail("test1234@test.com");

        // 담당자 조회권한, 1 - 개인권한 / 2 - 읽기권한 / 3 - 회사권한
        contactInfo.setSearchRole(3);

        try {

            Response response = taxinvoiceService.registContact(testCorpNum, contactInfo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "checkID", method = RequestMethod.GET)
    public String checkID(Model m) throws PopbillException {
        /*
         * 사용하고자 하는 아이디의 중복여부를 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#CheckID
         */

        try {

            Response response = taxinvoiceService.checkID(testUserID);
            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "response";
    }

    @RequestMapping(value = "getCorpInfo", method = RequestMethod.GET)
    public String getCorpInfo(Model m) throws PopbillException {
        /*
         * 연동회원의 회사정보를 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#GetCorpInfo
         */

        try {
            CorpInfo response = taxinvoiceService.getCorpInfo(testCorpNum);
            m.addAttribute("CorpInfo", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getCorpInfo";
    }

    @RequestMapping(value = "updateCorpInfo", method = RequestMethod.GET)
    public String updateCorpInfo(Model m) throws PopbillException {
        /*
         * 연동회원의 회사정보를 수정합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/member#UpdateCorpInfo
         */

        CorpInfo corpInfo = new CorpInfo();

        // 대표자 성명, 최대 100자
        corpInfo.setCeoname("대표자 성명 수정 테스트");

        // 회사명, 최대 200자
        corpInfo.setCorpName("회사명 수정 테스트");

        // 주소, 최대 300자
        corpInfo.setAddr("주소 수정 테스트");

        // 업태, 최대 100자
        corpInfo.setBizType("업태 수정 테스트");

        // 종목, 최대 100자
        corpInfo.setBizClass("종목 수정 테스트");

        try {
            Response response = taxinvoiceService.updateCorpInfo(testCorpNum, corpInfo);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

}
