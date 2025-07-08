/**
  * 팝빌 현금영수증 API Java SDK SpringBoot Example
  *
  * SpringBoot 연동 튜토리얼 안내 : https://developers.popbill.com/guide/cashbill/java/getting-started/tutorial?fwn=springboot
  * 연동 기술지원 연락처 : 1600-9854
  * 연동 기술지원 이메일 : code@linkhubcorp.com
  *
  */
package com.popbill.example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.popbill.api.BulkResponse;
import com.popbill.api.CBIssueResponse;
import com.popbill.api.CashbillService;
import com.popbill.api.ChargeInfo;
import com.popbill.api.EmailSendConfig;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.cashbill.BulkCashbillResult;
import com.popbill.api.cashbill.CBSearchResult;
import com.popbill.api.cashbill.Cashbill;
import com.popbill.api.cashbill.CashbillInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "CashbillService")
public class CashbillServiceController {

    @Autowired
    private CashbillService cashbillService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Cashbill/index";
    }

    @RequestMapping(value = "checkMgtKeyInUse", method = RequestMethod.GET)
    public String checkMgtKeyInUse(Model m) {
        /**
         * 파트너가 현금영수증 관리 목적으로 할당하는 문서번호 사용여부를 확인합니다.
         * - 이미 사용 중인 문서번호는 중복 사용이 불가하고, 현금영수증이 삭제된 경우에만 문서번호의 재사용이 가능합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/info#CheckMgtKeyInUse
         */

        // 현금영수증 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20220218-BOOT001";

        String isUseStr;

        try {
            boolean IsUse = cashbillService.checkMgtKeyInUse(CorpNum, mgtKey);
            isUseStr = (IsUse) ? "사용중" : "미사용중";
            m.addAttribute("Result", isUseStr);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "registIssue", method = RequestMethod.GET)
    public String registIssue(Model m) {
        /**
         * 작성된 현금영수증 데이터를 팝빌에 저장과 동시에 발행하여 "발행완료" 상태로 처리합니다.
         * - 현금영수증 국세청 전송 정책 [https://developers.popbill.com/guide/cashbill/java/introduction/policy-of-send-to-nts]
         * - https://developers.popbill.com/reference/cashbill/java/api/issue#RegistIssue
         */

        // 현금영수증 정보 객체
        Cashbill cashbill = new Cashbill();

        // 현금영수증 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        cashbill.setMgtKey("20230113-BOOT001");

        // 거래일시, 날짜(yyyyMMddHHmmss)
        // 당일, 전일만 가능
        cashbill.setTradeDT("20230112111111");

        // 문서형태, 승인거래 기재
        cashbill.setTradeType("승인거래");

        // 거래구분, {소득공제용, 지출증빙용} 중 기재
        cashbill.setTradeUsage("소득공제용");

        // 거래유형, {일반, 도서공연, 대중교통} 중 기재
        // - 미입력시 기본값 "일반" 처리
        cashbill.setTradeOpt("대중교통");

        // 과세형태, {과세, 비과세} 중 기재
        cashbill.setTaxationType("과세");

        // 합계금액, 숫자만 가능, 봉사료 + 공급가액 + 부가세
        cashbill.setTotalAmount("11000");

        // 공급가액, 숫자만 가능
        cashbill.setSupplyCost("10000");

        // 부가세, 양수 또는 0 입력
        cashbill.setTax("1000");

        // 봉사료, 양수 또는 0 입력
        cashbill.setServiceFee("0");

        // 가맹점 사업자번호, '-'제외 10자리
        cashbill.setFranchiseCorpNum("1234567890");

        // 가맹점 종사업장 번호
        cashbill.setFranchiseTaxRegID("");

        // 가맹점 상호
        cashbill.setFranchiseCorpName("가맹점 상호");

        // 가맹점 대표자 성명
        cashbill.setFranchiseCEOName("가맹점 대표자");

        // 가맹점 주소
        cashbill.setFranchiseAddr("가맹점 주소");

        // 가맹점 연락처
        cashbill.setFranchiseTEL("07043042991");

        // 식별번호, 거래구분에 따라 작성
        // └ 소득공제용 - 주민등록/휴대폰/카드번호(현금영수증 카드)/자진발급용 번호(010-000-1234) 기재가능
        // └ 지출증빙용 - 사업자번호/주민등록/휴대폰/카드번호(현금영수증 카드) 기재가능
        // └ 주민등록번호 13자리, 휴대폰번호 10~11자리, 카드번호 13~19자리, 사업자번호 10자리 입력 가능
        cashbill.setIdentityNum("0101112222");

        // 구매자 성명
        cashbill.setCustomerName("고객명");

        // 주문상품명
        cashbill.setItemName("상품명");

        // 주문번호
        cashbill.setOrderNumber("주문번호");

        // 구매자 이메일
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        cashbill.setEmail("test@test.com");

        // 구매자 휴대폰
        // - {smssendYN} 의 값이 true 인 경우 아래 휴대폰번호로 안내 문자 전송
        cashbill.setHp("");

        // 발행 안내 문자 전송여부
        cashbill.setSmssendYN(false);

        // 현금영수증 상태 이력을 관리하기 위한 메모
        String Memo = "현금영수증 즉시발행 메모";

        // 발행 안내 메일제목, 미기재시 기본 양식으로 메일 전송
        String emailSubject = "";

        try {
            CBIssueResponse response = cashbillService.registIssue(CorpNum, cashbill, Memo, UserID, emailSubject);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/issueResponse";
    }

    @RequestMapping(value = "bulkSubmit", method = RequestMethod.GET)
    public String bulkSubmit(Model m) {
        /**
         * 최대 100건의 현금영수증 발행을 한번의 요청으로 접수합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/issue#BulkSubmit
         */

        // 제출아이디, 대량 발행 접수를 구별하는 식별키
        // └ 최대 36자리 영문, 숫자, '-' 조합으로 구성
        String SubmitID = "20230102-BOOT-BULK";

        // 최대 100건.
        List<Cashbill> cashbillList = new ArrayList<Cashbill>();

        for (int i = 0 ; i < 5; i++) {

            // 현금영수증 정보 객체
            Cashbill cashbill = new Cashbill();

            // 현금영수증 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
            cashbill.setMgtKey(SubmitID + "-" + String.valueOf(i + 1));

            // 거래일시, 날짜(yyyyMMddHHmmss)
            // 당일, 전일만 가능
            cashbill.setTradeDT("20221104000000");

            // 문서형태, 승인거래 기재
            cashbill.setTradeType("승인거래");

            // 거래구분, {소득공제용, 지출증빙용} 중 기재
            cashbill.setTradeUsage("소득공제용");

            // 거래유형, {일반, 도서공연, 대중교통} 중 기재
            // - 미입력시 기본값 "일반" 처리
            cashbill.setTradeOpt("대중교통");

            // 과세형태, {과세, 비과세} 중 기재
            cashbill.setTaxationType("과세");

            // 합계금액, 숫자만 가능, 봉사료 + 공급가액 + 부가세
            cashbill.setTotalAmount("11000");

            // 공급가액, 숫자만 가능
            cashbill.setSupplyCost("10000");

            // 부가세, 양수 또는 0 입력
            cashbill.setTax("1000");

            // 봉사료, 양수 또는 0 입력
            cashbill.setServiceFee("0");

            // 가맹점 사업자번호, '-'제외 10자리
            cashbill.setFranchiseCorpNum("1234567890");

            // 가맹점 종사업장 번호
            cashbill.setFranchiseTaxRegID("");

            // 가맹점 상호
            cashbill.setFranchiseCorpName("가맹점 상호");

            // 가맹점 대표자 성명
            cashbill.setFranchiseCEOName("가맹점 대표자");

            // 가맹점 주소
            cashbill.setFranchiseAddr("가맹점 주소");

            // 가맹점 연락처
            cashbill.setFranchiseTEL("07043042991");

            // 식별번호, 거래구분에 따라 작성
            // └ 소득공제용 - 주민등록/휴대폰/카드번호(현금영수증 카드)/자진발급용 번호(010-000-1234) 기재가능
            // └ 지출증빙용 - 사업자번호/주민등록/휴대폰/카드번호(현금영수증 카드) 기재가능
            // └ 주민등록번호 13자리, 휴대폰번호 10~11자리, 카드번호 13~19자리, 사업자번호 10자리 입력 가능
            cashbill.setIdentityNum("0101112222");

            // 구매자 성명
            cashbill.setCustomerName("고객명");

            // 주문상품명
            cashbill.setItemName("상품명");

            // 주문번호
            cashbill.setOrderNumber("주문번호");

            // 구매자 이메일
            // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
            // 실제 거래처의 메일주소가 기재되지 않도록 주의
            cashbill.setEmail("");

            // 구매자 휴대폰
            // - {smssendYN} 의 값이 true 인 경우 아래 휴대폰번호로 안내 문자 전송
            cashbill.setHp("");

            // 발행 안내 문자 전송여부
            cashbill.setSmssendYN(false);

            cashbillList.add(cashbill);
        }

        try {
            BulkResponse response = cashbillService.bulkSubmit(CorpNum, SubmitID, cashbillList);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "bulkSubmitResponse";
    }

    @RequestMapping(value = "getBulkResult", method = RequestMethod.GET)
    public String getBulkResult(Model m) {
        /**
         * 접수시 기재한 SubmitID를 사용하여 현금영수증 접수결과를 확인합니다.
         * - 개별 현금영수증 처리상태는 접수상태(txState)가 완료(2) 시 반환됩니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/issue#GetBulkResult
         */

        // 초대량 발행 접수 시 기재한 제출아이디
        String SubmitID = "20221108-BOOT-BULK";

        try {
            BulkCashbillResult bulkResult = cashbillService.getBulkResult(CorpNum, SubmitID);
            m.addAttribute("BulkResult", bulkResult);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/GetBulkResult";
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String delete(Model m) {
        /**
         * 삭제 가능한 상태의 현금영수증을 삭제합니다.
         * - 삭제 가능한 상태: "전송실패"
         * - 현금영수증을 삭제하면 사용된 문서번호(mgtKey)를 재사용할 수 있습니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/issue#Delete
         */

        // 현금영수증 문서번호
        String mgtKey = "20220218-BOOT001";

        try {
            Response response = cashbillService.delete(CorpNum, mgtKey);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "revokeRegistIssue", method = RequestMethod.GET)
    public String revokeRegistIssue(Model m) {
        /**
         * 취소 현금영수증 데이터를 팝빌에 저장과 동시에 발행하여 "발행완료" 상태로 처리합니다.
         * - 현금영수증 국세청 전송 정책 [https://developers.popbill.com/guide/cashbill/java/introduction/policy-of-send-to-nts]
         * - https://developers.popbill.com/reference/cashbill/java/api/issue#RevokeRegistIssue
         */

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20220218-BOOT005";

        // 당초 국세청승인번호 - 상태확인(getInfo API) 함수를 통해 confirmNum 값 기재
        String orgConfirmNum = "820116333";

        // 당초 거래일자 - 상태확인(getInfo API) 함수를 통해 tradeDate 값 기재
        String orgTradeDate = "20230102";

        try {
            CBIssueResponse response = cashbillService.revokeRegistIssue(CorpNum, mgtKey, orgConfirmNum, orgTradeDate);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/issueResponse";
    }

    @RequestMapping(value = "revokeRegistIssue_part", method = RequestMethod.GET)
    public String revokeRegistIssue_part(Model m) {
        /**
         * 작성된 (부분)취소 현금영수증 데이터를 팝빌에 저장과 동시에 발행하여 "발행완료" 상태로 처리합니다.
         * - 취소 현금영수증의 금액은 원본 금액을 넘을 수 없습니다.
         * - 현금영수증 국세청 전송 정책 [https://developers.popbill.com/guide/cashbill/java/introduction/policy-of-send-to-nts]
         * - https://developers.popbill.com/reference/cashbill/java/api/issue#RevokeRegistIssue
         */

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT006";

        // 당초 국세청승인번호 - 상태확인(getInfo API) 함수를 통해 confirmNum 값 기재
        String orgConfirmNum = "820116333";

        // 당초 거래일자 - 상태확인(getInfo API) 함수를 통해 tradeDate 값 기재
        String orgTradeDate = "20230102";

        // 안내 문자 전송여부 , true / false 중 택 1
        // └ true = 전송 , false = 미전송
        // └ 당초 승인 현금영수증의 구매자(고객)의 휴대폰번호 문자 전송
        Boolean smssendYN = false;

        // 발행 메모
        String memo = "취소 현금영수증 발행 메모";

        // 현금영수증 취소유형 , true / false 중 택 1
        // └ true = 부분 취소 , false = 전체 취소
        // └ 미입력시 기본값 false 처리
        Boolean isPartCancel = true;

        // 취소사유 , 1 / 2 / 3 중 택 1
        // └ 1 = 거래취소 , 2 = 오류발급취소 , 3 = 기타
        // └ 미입력시 기본값 1 처리
        Integer cancelType = 1;

        // [취소] 공급가액
        // - 현금영수증 취소유형이 true 인 경우 취소할 공급가액 입력
        // - 현금영수증 취소유형이 false 인 경우 미입력
        String supplyCost = "3000";

        // [취소] 부가세
        // - 현금영수증 취소유형이 true 인 경우 취소할 부가세 입력
        // - 현금영수증 취소유형이 false 인 경우 미입력
        String tax = "300";

        // [취소] 봉사료
        // - 현금영수증 취소유형이 true 인 경우 취소할 봉사료 입력
        // - 현금영수증 취소유형이 false 인 경우 미입력
        String serviceFee = "0";

        // [취소] 거래금액 (공급가액+부가세+봉사료)
        // - 현금영수증 취소유형이 true 인 경우 취소할 거래금액 입력
        // - 현금영수증 취소유형이 false 인 경우 미입력
        String totalAmount = "3300";

        try {
            CBIssueResponse response = cashbillService.revokeRegistIssue(CorpNum, mgtKey, orgConfirmNum, orgTradeDate,
                    smssendYN, memo, isPartCancel, cancelType, supplyCost, tax, serviceFee, totalAmount);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/issueResponse";
    }

    @RequestMapping(value = "getInfo", method = RequestMethod.GET)
    public String getInfo(Model m) {
        /**
         * 현금영수증 1건의 상태 및 요약정보를 확인합니다.
         * - 리턴값 'CashbillInfo'의 변수 'stateCode'를 통해 현금영수증의 상태코드를 확인합니다.
         * - 현금영수증 상태코드 [https://developers.popbill.com/reference/cashbill/java/response-code]
         * - https://developers.popbill.com/reference/cashbill/java/api/info#GetInfo
         */

        // 현금영수증 문서번호
        String mgtKey = "20220218-BOOT002";

        try {
            CashbillInfo cashbillInfo = cashbillService.getInfo(CorpNum, mgtKey);
            m.addAttribute("CashbillInfo", cashbillInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/CashbillInfo";
    }

    @RequestMapping(value = "getInfos", method = RequestMethod.GET)
    public String getInfos(Model m) {
        /**
         * 다수건의 현금영수증 상태 및 요약 정보를 확인합니다. (1회 호출 시 최대 1,000건 확인 가능)
         * - 리턴값 'CashbillInfo'의 변수 'stateCode'를 통해 현금영수증의 상태코드를 확인합니다.
         * - 현금영수증 상태코드 [https://developers.popbill.com/reference/cashbill/java/response-code]
         * - https://developers.popbill.com/reference/cashbill/java/api/info#GetInfos
         */

        // 현금영수증 문서번호 배열 (최대 1000건)
        String[] mgtKeyList = new String[] { "20221108-BOOT002", "20220218-BOOT004", "20220218-BOOT005" };

        try {
            CashbillInfo[] cashbillInfos = cashbillService.getInfos(CorpNum, mgtKeyList);
            m.addAttribute("CashbillInfos", cashbillInfos);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/CashbillInfo";
    }

    @RequestMapping(value = "getDetailInfo", method = RequestMethod.GET)
    public String getDetailInfo(Model m) {
        /**
         * 현금영수증 1건의 상세정보를 확인합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/info#GetDetailInfo
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT006";

        try {
            Cashbill cashbill = cashbillService.getDetailInfo(CorpNum, mgtKey);
            m.addAttribute("Cashbill", cashbill);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/Cashbill";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /**
         * 검색조건에 해당하는 현금영수증을 조회합니다 (조회기간 단위 : 최대 6개월).
         * 현금영수증 상태코드 [https://developers.popbill.com/reference/cashbill/java/response-code]
         * - https://developers.popbill.com/reference/cashbill/java/api/info#Search
         */

        // 일자 유형 ("R" , "T" , "I" 중 택 1)
        // └ R = 등록일자 , T = 거래일자 , I = 발행일자
        String DType = "T";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20230102";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20230131";

        // 상태코드 배열 (2,3번째 자리에 와일드카드(*) 사용 가능)
        // - 미입력시 전체조회
        String[] State = { "3**"};

        // 문서형태 배열 ("N" , "C" 중 선택, 다중 선택 가능)
        // - N = 일반 현금영수증 , C = 취소 현금영수증
        // - 미입력시 전체조회
        String[] TradeType = { "N", "C" };

        // 거래구분 배열 ("P" , "C" 중 선택, 다중 선택 가능)
        // - P = 소득공제용 , C = 지출증빙용
        // - 미입력시 전체조회
        String[] TradeUsage = { "P", "C" };

        // 거래유형 배열 ("N" , "B" , "T" 중 선택, 다중 선택 가능)
        // - N = 일반 , B = 도서공연 , T = 대중교통
        // - 미입력시 전체조회
        String[] TradeOpt = { "N", "B", "T" };

        // 과세형태 배열 ("T" , "N" 중 선택, 다중 선택 가능)
        // - T = 과세 , N = 비과세
        // - 미입력시 전체조회
        String[] TaxationType = { "T", "N" };

        // 식별번호 조회 (미기재시 전체조회)
        String QString = "";

        // 페이지 번호
        int Page = 1;

        // 페이지당 목록 건수, 최대 1000건
        int PerPage = 20;

        // 정렬방향, A-오름차순, D-내림차순
        String Order = "D";

        // 가맹점 종사업장 번호
        // └ 다수건 검색시 콤마(",")로 구분. 예) "1234,1000"
        // └ 미입력시 전제조회
        String FranchiseTaxRegID = "";

        try {
            CBSearchResult searchResult = cashbillService.search(CorpNum, DType, SDate, EDate, State, TradeType,
                    TradeUsage, TradeOpt, TaxationType, QString, Page, PerPage, Order, FranchiseTaxRegID);
            m.addAttribute("SearchResult", searchResult);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/SearchResult";
    }

    @RequestMapping(value = "getURL", method = RequestMethod.GET)
    public String getURL(Model m) {
        /**
         * 로그인 상태로 팝빌 사이트의 현금영수증 문서함 메뉴에 접근할 수 있는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/info#GetURL
         */

        // TBOX : 임시문서함 , PBOX : 발행문서함, WRITE : 현금영수증 작성
        String TOGO = "WRITE";

        try {
            String url = cashbillService.getURL(CorpNum, UserID, TOGO);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPopUpURL", method = RequestMethod.GET)
    public String getPopUpURL(Model m) {
        /**
         * 현금영수증 1건의 상세 정보 페이지의 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/view#GetPopUpURL
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            String url = cashbillService.getPopUpURL(CorpNum, mgtKey, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getViewURL", method = RequestMethod.GET)
    public String getViewURL(Model m) {
        /**
         * 현금영수증 1건의 상세 정보 페이지(사이트 상단, 좌측 메뉴 및 버튼 제외)의 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/view#GetViewURL
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            String url = cashbillService.getViewURL(CorpNum, mgtKey, UserID);
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
         * 현금영수증 1건을 인쇄하기 위한 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/view#GetPrintURL
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            String url = cashbillService.getPrintURL(CorpNum, mgtKey, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getMassPrintURL", method = RequestMethod.GET)
    public String getMassPrintURL(Model m) {
        /**
         * 다수건의 현금영수증을 인쇄하기 위한 페이지의 팝업 URL을 반환합니다. (최대 100건)
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/view#GetMassPrintURL
         */

        // 문서번호 배열, 최대 100건
        String[] mgtKeyList = new String[] { "20230102-BOOT002", "20230102-BOOT003", "20230102-BOOT004" };

        try {
            String url = cashbillService.getMassPrintURL(CorpNum, mgtKeyList, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getMailURL", method = RequestMethod.GET)
    public String getMailURL(Model m) {
        /**
         * 현금영수증 안내메일의 상세보기 링크 URL을 반환합니다.
         * - 함수 호출로 반환 받은 URL에는 유효시간이 없습니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/view#GetMailURL
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            String url = cashbillService.getMailURL(CorpNum, mgtKey, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPDFURL", method = RequestMethod.GET)
    public String getPDFURL(Model m) {
        /**
         * 현금영수증 PDF 파일을 다운 받을 수 있는 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/view#GetPDFURL
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            String url = cashbillService.getPDFURL(CorpNum, mgtKey, UserID);
            m.addAttribute("Result", url);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendEmail", method = RequestMethod.GET)
    public String sendEmail(Model m) {
        /**
         * 현금영수증과 관련된 안내 메일을 재전송 합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/etc#SendEmail
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        // 수신자 메일주소
        String receiver = "test@test.com";

        try {
            Response response = cashbillService.sendEmail(CorpNum, mgtKey, receiver);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "sendSMS", method = RequestMethod.GET)
    public String sendSMS(Model m) {
        /**
         * 현금영수증과 관련된 안내 SMS(단문) 문자를 재전송하는 함수로, 팝빌 사이트 [문자·팩스] > [문자] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다.
         * - 메시지는 최대 90byte까지 입력 가능하고, 초과한 내용은 자동으로 삭제되어 전송합니다. (한글 최대 45자)
         * - 함수 호출 시 포인트가 과금됩니다. (전송실패시 환불처리)
         * - https://developers.popbill.com/reference/cashbill/java/api/etc#SendSMS
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        // 발신번호
        String sender = "07043042991";

        // 수신번호
        String receiver = "010111222";

        // 문자 전송 내용 (90Byte 초과시 길이가 조정되어 전송)
        String contents = "현금영수증 문자메시지 전송 테스트입니다.";

        try {
            Response response = cashbillService.sendSMS(CorpNum, mgtKey, sender, receiver, contents);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "sendFAX", method = RequestMethod.GET)
    public String sendFAX(Model m) {
        /**
         * 현금영수증을 팩스로 전송하는 함수로, 팝빌 사이트 [문자·팩스] > [팩스] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다.
         * - 함수 호출 시 포인트가 과금됩니다. (전송실패시 환불처리)
         * - https://developers.popbill.com/reference/cashbill/java/api/etc#SendFAX
         */

        // 현금영수증 문서번호
        String mgtKey = "20230102-BOOT002";

        // 발신자 번호
        String sender = "07043042991";

        // 수신자 팩스번호
        String receiver = "010111222";

        try {
            Response response = cashbillService.sendFAX(CorpNum, mgtKey, sender, receiver);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "assignMgtKey", method = RequestMethod.GET)
    public String assignMgtKey(Model m) {
        /**
         * 팝빌 사이트를 통해 발행하여 문서번호가 부여되지 않은 현금영수증에 문서번호를 할당합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/etc#AssignMgtKey
         */

        // 현금영수증 팝빌번호, 문서 목록조회(Search API) 함수의 반환항목 중 ItemKey 참조
        String itemKey = "021080716195300001";

        // 현금영수증 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT007";

        try {
            Response response = cashbillService.assignMgtKey(CorpNum, itemKey, mgtKey);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "listEmailConfig", method = RequestMethod.GET)
    public String listEmailConfig(Model m) {
        /**
         * 현금영수증 관련 메일 항목에 대한 발송설정을 확인합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/etc#ListEmailConfig
         */
        Map<String, Boolean> emailSendConfigs = new HashMap<String, Boolean>();

        try {
            EmailSendConfig[] Configs = cashbillService.listEmailConfig(CorpNum);

            for (EmailSendConfig emailSendConfig : Configs) {
                emailSendConfigs.put(emailSendConfig.getEmailType(), emailSendConfig.getSendYN());
            }

            m.addAttribute("EmailSendConfigs", emailSendConfigs);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Cashbill/EmailSendConfig";
    }

    @RequestMapping(value = "updateEmailConfig", method = RequestMethod.GET)
    public String updateEmailConfig(Model m) {
        /**
         * 현금영수증 관련 메일 항목에 대한 발송설정을 수정합니다.
         * - https://developers.popbill.com/reference/cashbill/java/api/etc#UpdateEmailConfig
         *
         * 메일전송유형
         * - CSH_ISSUE : 고객에게 현금영수증이 발행 되었음을 알려주는 메일 입니다.
         * - CSH_CANCEL : 고객에게 현금영수증이 발행취소 되었음을 알려주는 메일 입니다.
         */

        // 메일 전송 유형
        String emailType = "CSH_ISSUE";

        // 전송 여부 (true = 전송, false = 미전송)
        Boolean sendYN = true;

        try {
            Response response = cashbillService.updateEmailConfig(CorpNum, emailType, sendYN);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /**
         * 현금영수증 발행시 과금되는 포인트 단가를 확인합니다.
         * - https://developers.popbill.com/reference/cashbill/java/common-api/point#GetUnitCost
         */

        try {
            float unitCost = cashbillService.getUnitCost(CorpNum);
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
         * 팝빌 현금영수증 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/cashbill/java/common-api/point#GetChargeInfo
         */

        try {
            ChargeInfo chrgInfo = cashbillService.getChargeInfo(CorpNum);
            m.addAttribute("ChargeInfo", chrgInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

}