package com.popbill.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.ContactInfo;
import com.popbill.api.CorpInfo;
import com.popbill.api.JoinForm;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.TaxinvoiceService;

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

    @RequestMapping(value = "getPartnerURL", method = RequestMethod.GET)
    public String getPartnerURL(Model m) throws PopbillException {
        /*
         * 파트너 포인트 충전을 위한 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
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
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
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
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
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
