package com.popbill.example.controller;

import com.popbill.api.*;
import com.popbill.api.taxinvoice.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
@RequestMapping(value = "TaxinvoiceService")
public class TaxinvoiceServiceController {

    @Autowired
    private TaxinvoiceService taxinvoiceService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Taxinvoice/index";
    }

    @RequestMapping(value = "checkMgtKeyInUse", method = RequestMethod.GET)
    public String checkMgtKeyInUse(Model m) {
        /**
         * 파트너가 세금계산서 관리 목적으로 할당하는 문서번호의 사용여부를 확인합니다.
         * - 이미 사용 중인 문서번호는 중복 사용이 불가하고, 세금계산서가 삭제된 경우에만 문서번호의 재사용이 가능합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/info#CheckMgtKeyInUse
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType keyType = MgtKeyType.SELL;

        // 세금계산서 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT001";

        String isUseStr;

        try {

            boolean IsUse = taxinvoiceService.checkMgtKeyInUse(CorpNum, keyType, mgtKey);

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
         * 작성된 세금계산서 데이터를 팝빌에 저장과 동시에 발행(전자서명)하여 "발행완료" 상태로 처리합니다.
         * - 세금계산서 국세청 전송 정책 [https://developers.popbill.com/guide/taxinvoice/java/introduction/policy-of-send-to-nts]
         * - "발행완료"된 전자세금계산서는 국세청 전송 이전에 발행취소(CancelIssue API) 함수로 국세청 신고 대상에서 제외할 수 있습니다.
         * - 임시저장(Register API) 함수와 발행(Issue API) 함수를 한 번의 프로세스로 처리합니다.
         * - 세금계산서 발행을 위해서 공급자의 인증서가 팝빌 인증서버에 사전등록 되어야 합니다.
         * └ 위수탁발행의 경우, 수탁자의 인증서 등록이 필요합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/issue#RegistIssue
         */

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20230102");

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // 세액 합계
        taxinvoice.setTaxTotal("10000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        /**********************************************************************
         * 추가담당자 정보 - 세금계산서 발행 안내 메일을 수신받을 공급받는자 담당자가 다수인 경우 - 담당자 정보를 추가하여 발행 안내메일을 다수에게 전송할 수
         * 있습니다. (최대 5명)
         *********************************************************************/

        // taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        // TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        // addContact.setSerialNum(1);
        // addContact.setContactName("추가 담당자 성명");
        // addContact.setEmail("test2@test.com");

        // taxinvoice.getAddContactList().add(addContact);

        // 거래명세서 동시작성여부 (true / false 중 택 1)
        // └ true = 사용 , false = 미사용
        // - 미입력 시 기본값 false 처리
        Boolean WriteSpecification = false;

        // {writeSpecification} = true인 경우, 거래명세서 문서번호 할당
        // - 미입력시 기본값 세금계산서 문서번호와 동일하게 할당
        String DealInvoiceKey = null;

        // 즉시발행 메모
        String Memo = "즉시발행 메모";

        // 지연발행 강제여부 (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        // true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        try {

            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice,
                    WriteSpecification, Memo, ForceIssue, DealInvoiceKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/issueResponse";
    }

    @RequestMapping(value = "bulkSubmit", method = RequestMethod.GET)
    public String bulkSubmit(Model m) {
        /**
         * 최대 100건의 세금계산서 발행을 한번의 요청으로 접수합니다.
         * - 세금계산서 발행을 위해서 공급자의 인증서가 팝빌 인증서버에 사전등록 되어야 합니다.
         *   └ 위수탁발행의 경우, 수탁자의 인증서 등록이 필요합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/issue#BulkSubmit
         */

        // 제출아이디, 대량 발행 접수를 구별하는 식별키
        // └ 최대 36자리 영문, 숫자, '-' 조합으로 구성
        String SubmitID = "20230102-BOOT-BULK";

        // 지연발행 강제여부
        // 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        // true로 선언하여 API를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        // 최대 100건
        List<Taxinvoice> bulkTx = new ArrayList<Taxinvoice>();

        for (int i = 0; i < 100; i++) {

            // 세금계산서 정보 객체
            Taxinvoice taxinvoice = new Taxinvoice();

            // 작성일자, 날짜형식(yyyyMMdd)
            taxinvoice.setWriteDate("20220218");

            // 과금방향, [정과금, 역과금] 중 선택기재
            // - 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
            // -'역과금'은 역발행 세금계산서 발행 시에만 이용가능
            taxinvoice.setChargeDirection("정과금");

            // 발행유형, [정발행, 역발행, 위수탁] 중 기재
            taxinvoice.setIssueType("정발행");

            // [영수, 청구, 없음] 중 기재
            taxinvoice.setPurposeType("영수");

            // 과세형태, [과세, 영세, 면세] 중 기재
            taxinvoice.setTaxType("과세");

            /**********************************************************************
             * 공급자 정보
             *********************************************************************/

            // 공급자 사업자번호
            taxinvoice.setInvoicerCorpNum(CorpNum);

            // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
            taxinvoice.setInvoicerTaxRegID("");

            // 공급자 상호
            taxinvoice.setInvoicerCorpName("공급자 상호");

            // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
            taxinvoice.setInvoicerMgtKey(SubmitID + "-" + String.valueOf(i + 1));

            // 공급자 대표자 성명
            taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

            // 공급자 주소
            taxinvoice.setInvoicerAddr("공급자 주소");

            // 공급자 종목
            taxinvoice.setInvoicerBizClass("공급자 종목");

            // 공급자 업태
            taxinvoice.setInvoicerBizType("공급자 업태,업태2");

            // 공급자 담당자 성명
            taxinvoice.setInvoicerContactName("공급자 담당자 성명");

            // 공급자 담당자 메일주소
            taxinvoice.setInvoicerEmail("");

            // 공급자 담당자 연락처
            taxinvoice.setInvoicerTEL("070-7070-0707");

            // 공급자 담당자 휴대폰번호
            taxinvoice.setInvoicerHP("010-000-2222");

            // 발행 안내 문자 전송여부 (true / false 중 택 1)
            // └ true = 전송 , false = 미전송
            // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
            // - 전송 시 포인트 차감되며, 전송실패시 환불처리
            taxinvoice.setInvoicerSMSSendYN(false);

            /**********************************************************************
             * 공급받는자 정보
             *********************************************************************/

            // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
            taxinvoice.setInvoiceeType("사업자");

            // 공급받는자 사업자번호
            // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
            // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
            // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
            taxinvoice.setInvoiceeCorpNum("8888888888");

            // 공급받는자 상호
            taxinvoice.setInvoiceeCorpName("공급받는자 상호");

            // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
            taxinvoice.setInvoiceeMgtKey("");

            // 공급받는자 대표자 성명
            taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

            // 공급받는자 주소
            taxinvoice.setInvoiceeAddr("공급받는자 주소");

            // 공급받는자 종목
            taxinvoice.setInvoiceeBizClass("공급받는자 업종");

            // 공급받는자 업태
            taxinvoice.setInvoiceeBizType("공급받는자 업태");

            // 공급받는자 담당자 성명
            taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

            // 공급받는자 담당자 메일주소
            // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
            // 실제 거래처의 메일주소가 기재되지 않도록 주의
            taxinvoice.setInvoiceeEmail1("");

            // 공급받는자 담당자 연락처
            taxinvoice.setInvoiceeTEL1("070-111-222");

            // 공급받는자 담당자 휴대폰번호
            taxinvoice.setInvoiceeHP1("010-111-222");

            // 역발행 안내 문자 전송여부 (true / false 중 택 1)
            // └ true = 전송 , false = 미전송
            // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
            // - 전송 시 포인트 차감되며, 전송실패시 환불처리
            taxinvoice.setInvoiceeSMSSendYN(false);

            /**********************************************************************
             * 세금계산서 기재정보
             *********************************************************************/

            // 공급가액 합계
            taxinvoice.setSupplyCostTotal("100000");

            // 세액 합계
            taxinvoice.setTaxTotal("10000");

            // 합계금액, 공급가액 + 세액
            taxinvoice.setTotalAmount("110000");

            // 일련번호
            taxinvoice.setSerialNum("123");

            // 현금
            taxinvoice.setCash("");

            // 수표
            taxinvoice.setChkBill("");

            // 어음
            taxinvoice.setNote("");

            // 외상미수금
            taxinvoice.setCredit("");

            // 비고
            // {invoiceeType}이 "외국인" 이면 remark1 필수
            // - 외국인 등록번호 또는 여권번호 입력
            taxinvoice.setRemark1("비고1");
            taxinvoice.setRemark2("비고2");
            taxinvoice.setRemark3("비고3");

            // 책번호 '권' 항목, 최대값 32767
            taxinvoice.setKwon((short) 1);

            // 책번호 '호' 항목, 최대값 32767
            taxinvoice.setHo((short) 1);

            // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
            // └ true = 첨부 , false = 미첨부(기본값)
            // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
            taxinvoice.setBusinessLicenseYN(false);

            // 통장사본 이미지 첨부여부 (true / false 중 택 1)
            // └ true = 첨부 , false = 미첨부(기본값)
            // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
            taxinvoice.setBankBookYN(false);

            /**********************************************************************
             * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
             * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
             *********************************************************************/
            // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
            taxinvoice.setModifyCode(null);

            // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
            taxinvoice.setOrgNTSConfirmNum(null);

            /**********************************************************************
             * 상세항목(품목) 정보
             *********************************************************************/

            taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

            // 상세항목 객체
            TaxinvoiceDetail detail = new TaxinvoiceDetail();

            detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
            detail.setPurchaseDT("20230102"); // 거래일자
            detail.setItemName("품목명"); // 품목명
            detail.setSpec("규격"); // 규격
            detail.setQty("1"); // 수량
            detail.setUnitCost("50000"); // 단가
            detail.setSupplyCost("50000"); // 공급가액
            detail.setTax("5000"); // 세액
            detail.setRemark("품목비고"); // 비고

            taxinvoice.getDetailList().add(detail);

            detail = new TaxinvoiceDetail();

            detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
            detail.setPurchaseDT("20230102"); // 거래일자
            detail.setItemName("품목명2"); // 품목명
            detail.setSpec("규격"); // 규격
            detail.setQty("1"); // 수량
            detail.setUnitCost("50000"); // 단가
            detail.setSupplyCost("50000"); // 공급가액
            detail.setTax("5000"); // 세액
            detail.setRemark("품목비고2"); // 비고

            taxinvoice.getDetailList().add(detail);

            /**********************************************************************
             * 추가담당자 정보 - 세금계산서 발행 안내 메일을 수신받을 공급받는자 담당자가 다수인 경우 - 담당자 정보를 추가하여 발행 안내메일을 다수에게 전송할 수
             * 있습니다. (최대 5명)
             *********************************************************************/

            taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

            TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

            addContact.setSerialNum(1); // 일련번호 (1부터 순차적으로 입력 (최대 5))
            addContact.setContactName("추가 담당자 성명"); // 담당자 성명
            addContact.setEmail(""); // 이메일

            taxinvoice.getAddContactList().add(addContact);

            bulkTx.add(taxinvoice);
        }

        try {

            BulkResponse response =
                    taxinvoiceService.bulkSubmit(CorpNum, SubmitID, bulkTx, ForceIssue);

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
         * 접수시 기재한 SubmitID를 사용하여 세금계산서 접수결과를 확인합니다. - 개별 세금계산서 처리상태는 접수상태(txState)가 완료(2) 시 반환됩니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/issue#GetBulkResult
         */

        // 대량 발행 접수시 기재한 제출아이디
        String SubmitID = "20230102-BOOT-BULK";

        try {

            BulkTaxinvoiceResult bulkResult = taxinvoiceService.getBulkResult(CorpNum, SubmitID);

            m.addAttribute("BulkResult", bulkResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/GetBulkResult";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register(Model m) {
        /**
         * 작성된 세금계산서 데이터를 팝빌에 저장합니다.
         * - "임시저장" 상태의 세금계산서는 발행(Issue) 함수를 호출하여 "발행완료" 처리한 경우에만 국세청으로 전송됩니다.
         * - 정발행 시 임시저장(Register)과 발행(Issue)을 한번의 호출로 처리하는 즉시발행(RegistIssue API) 프로세스 연동을 권장합니다.
         * - 역발행 시 임시저장(Register)과 역발행요청(Request)을 한번의 호출로 처리하는 즉시요청(RegistRequest API) 프로세스 연동을 권장합니다.
         * - 세금계산서 파일첨부 기능을 구현하는 경우, 임시저장(Register API) -> 파일첨부(AttachFile API) -> 발행(Issue API) 함수를 차례로 호출합니다.
         * - 역발행 세금계산서를 저장하는 경우, 객체 'Taxinvoice'의 변수 'chargeDirection' 값을 통해 과금 주체를 지정할 수 있습니다.
         *   └ 정과금 : 공급자 과금 , 역과금 : 공급받는자 과금
         * - 임시저장된 세금계산서는 팝빌 사이트 '임시문서함'에서 확인 가능합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/issue#Register
         */

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20230102");

        // 과금방향, [정과금, 역과금] 중 선택기재
        // - 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -'역과금'은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT002");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@test.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // 세액 합계
        taxinvoice.setTaxTotal("10000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        /**********************************************************************
         * 추가담당자 정보 - 세금계산서 발행 안내 메일을 수신받을 공급받는자 담당자가 다수인 경우 - 담당자 정보를 추가하여 발행 안내메일을 다수에게 전송할 수
         * 있습니다. (최대 5명)
         *********************************************************************/

        taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        addContact.setSerialNum(1); // 일련번호 (1부터 순차적으로 입력 (최대 5))
        addContact.setContactName("추가 담당자 성명"); // 담당자 성명
        addContact.setEmail("test2@test.com"); // 이메일

        taxinvoice.getAddContactList().add(addContact);

        try {

            Response response = taxinvoiceService.register(CorpNum, taxinvoice);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String update(Model m) {
        /**
         * "임시저장" 상태의 세금계산서를 수정합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#Update
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20230102");

        // 과금방향, [정과금, 역과금] 중 선택기재
        // - 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -'역과금'은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호 (하이픈 '-' 제외 10 자리)
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명_수정");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소_수정");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // 공급받는자 문서번호, 역발행 시 필수
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // 세액 합계
        taxinvoice.setTaxTotal("10000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        /**********************************************************************
         * 추가담당자 정보 - 세금계산서 발행 안내 메일을 수신받을 공급받는자 담당자가 다수인 경우 - 담당자 정보를 추가하여 발행 안내메일을 다수에게 전송할 수
         * 있습니다. (최대 5명)
         *********************************************************************/

        taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        addContact.setSerialNum(1); // 일련번호 (1부터 순차적으로 입력 (최대 5))
        addContact.setContactName("추가 담당자 성명"); // 담당자 성명
        addContact.setEmail("test2@test.com"); // 이메일

        taxinvoice.getAddContactList().add(addContact);

        try {

            Response response = taxinvoiceService.update(CorpNum, mgtKeyType, mgtKey, taxinvoice);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "issue", method = RequestMethod.GET)
    public String issue(Model m) {
        /**
         * "임시저장" 또는 "(역)발행대기" 상태의 세금계산서를 발행(전자서명)하며, "발행완료" 상태로 처리합니다.
         * - 세금계산서 국세청 전송정책 [https://developers.popbill.com/guide/taxinvoice/java/introduction/policy-of-send-to-nts]
         * - "발행완료" 된 전자세금계산서는 국세청 전송 이전에 발행취소(CancelIssue API) 함수로 국세청 신고 대상에서 제외할 수 있습니다.
         * - 세금계산서 발행을 위해서 공급자의 인증서가 팝빌 인증서버에 사전등록 되어야 합니다.
         *   └ 위수탁발행의 경우, 수탁자의 인증서 등록이 필요합니다.
         * - 세금계산서 발행 시 공급받는자에게 발행 메일이 발송됩니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/issue#Issue
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 메모
        String memo = "발행 메모";

        // 지연발행 강제여부 (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        // true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean forceIssue = false;

        try {

            IssueResponse response = taxinvoiceService.issue(CorpNum, mgtKeyType, mgtKey, memo,
                    forceIssue, UserID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/issueResponse";
    }

    @RequestMapping(value = "cancelIssue", method = RequestMethod.GET)
    public String cancelIssue(Model m) {
        /**
         * 국세청 전송 이전 "발행완료" 상태의 세금계산서를 "발행취소"하고 국세청 전송 대상에서 제외합니다. - 삭제(Delete API) 함수를 호출하여 "발행취소"
         * 상태의 전자세금계산서를 삭제하면, 문서번호 재사용이 가능합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#CancelIssue
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 메모
        String memo = "발행취소 메모";

        try {

            Response response = taxinvoiceService.cancelIssue(CorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String delete(Model m) {
        /**
         * 삭제 가능한 상태의 세금계산서를 삭제합니다. - 삭제 가능한 상태: "임시저장", "발행취소", "역발행거부", "역발행취소", "전송실패" - 삭제처리된
         * 세금계산서의 문서번호는 재사용이 가능합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#Delete
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            Response response = taxinvoiceService.delete(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "registRequest", method = RequestMethod.GET)
    public String registRequest(Model m) {
        /**
         * 공급받는자가 작성한 세금계산서 데이터를 팝빌에 저장하고 공급자에게 송부하여 발행을 요청합니다. - 역발행 세금계산서 프로세스를 구현하기 위해서는
         * 공급자/공급받는자가 모두 팝빌에 회원이여야 합니다. - 발행 요청된 세금계산서는 "(역)발행대기" 상태이며, 공급자가 팝빌 사이트 또는 함수를 호출하여 발행한
         * 경우에만 국세청으로 전송됩니다. - 공급자는 팝빌 사이트의 "매출 발행 대기함"에서 발행대기 상태의 역발행 세금계산서를 확인할 수 있습니다. -
         * 임시저장(Register API) 함수와 역발행 요청(Request API) 함수를 한 번의 프로세스로 처리합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#RegistRequest
         */

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20230102");

        // 과금방향, [정과금, 역과금] 중 선택기재
        // - 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -'역과금'은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("역발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum("8888888888");

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        // - 역발행 요청 시 공급자에게 역발행 요청 메일 발송
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum(CorpNum);

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("20230102-BOOT003");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // 세액 합계
        taxinvoice.setTaxTotal("10000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 일련번호
        taxinvoice.setSerialNum("");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/
        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();
        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 메모
        String Memo = "즉시요청 메모";

        try {

            Response response = taxinvoiceService.registRequest(CorpNum, taxinvoice, Memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "request", method = RequestMethod.GET)
    public String request(Model m) {
        /**
         * 공급받는자가 저장된 역발행 세금계산서를 공급자에게 송부하여 발행 요청합니다. - 역발행 세금계산서 프로세스를 구현하기 위해서는 공급자/공급받는자가 모두 팝빌에
         * 회원이여야 합니다. - 역발행 요청된 세금계산서는 "(역)발행대기" 상태이며, 공급자가 팝빌 사이트 또는 함수를 호출하여 발행한 경우에만 국세청으로 전송됩니다.
         * - 공급자는 팝빌 사이트의 "매출 발행 대기함"에서 발행대기 상태의 역발행 세금계산서를 확인할 수 있습니다. - 역발행 요청시 공급자에게 역발행 요청 메일이
         * 발송됩니다. - 공급자가 역발행 세금계산서 발행시 포인트가 과금됩니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#Request
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.BUY;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT004";

        // 메모
        String memo = "역발행 요청 메모";

        try {

            Response response = taxinvoiceService.request(CorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "cancelRequest", method = RequestMethod.GET)
    public String cancelRequest(Model m) {
        /**
         * 공급자가 요청받은 역발행 세금계산서를 발행하기 전, 공급받는자가 역발행요청을 취소합니다. - 함수 호출시 상태 값이 "취소"로 변경되고, 해당 역발행
         * 세금계산서는 공급자에 의해 발행 될 수 없습니다. - [취소]한 세금계산서의 문서번호를 재사용하기 위해서는 삭제 (Delete API) 함수를 호출해야 합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/issue#CancelRequest
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.BUY;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT004";

        // 메모
        String memo = "역발행 취소 메모";

        try {

            Response response = taxinvoiceService.cancelRequest(CorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "refuse", method = RequestMethod.GET)
    public String refuse(Model m) {
        /**
         * 공급자가 공급받는자에게 역발행 요청 받은 세금계산서의 발행을 거부합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#Refuse
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT005";

        // 메모
        String memo = "역발행 거부 메모";

        try {

            Response response = taxinvoiceService.refuse(CorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "sendToNTS", method = RequestMethod.GET)
    public String sendToNTS(Model m) {
        /**
         * 공급자가 "발행완료" 상태의 전자세금계산서를 국세청에 즉시 전송하며, 함수 호출 후 최대 30분 이내에 전송 처리가 완료됩니다. - 국세청 즉시전송을 호출하지
         * 않은 세금계산서는 발행일 기준 다음 영업일 오후 3시에 팝빌 시스템에서 일괄적으로 국세청으로 전송합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/issue#SendToNTS
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {

            Response response = taxinvoiceService.sendToNTS(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getInfo", method = RequestMethod.GET)
    public String getInfo(Model m) {
        /**
         * 세금계산서 1건의 상태 및 요약정보를 확인합니다. - 리턴값 'TaxinvoiceInfo'의 변수 'stateCode'를 통해 세금계산서의 상태코드를
         * 확인합니다. - 세금계산서 상태코드
         * [https://developers.popbill.com/reference/taxinvoice/java/response-code] -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#GetInfo
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {

            TaxinvoiceInfo taxinvoiceInfo = taxinvoiceService.getInfo(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("TaxinvoiceInfo", taxinvoiceInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceInfo";
    }

    @RequestMapping(value = "getInfos", method = RequestMethod.GET)
    public String getInfos(Model m) {
        /**
         * 다수건의 세금계산서 상태 및 요약 정보를 확인합니다. (1회 호출 시 최대 1,000건 확인 가능) - 리턴값 'TaxinvoiceInfo'의 변수
         * 'stateCode'를 통해 세금계산서의 상태코드를 확인합니다. - 세금계산서 상태코드
         * [https://developers.popbill.com/reference/taxinvoice/java/response-code] -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#GetInfos
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호 배열 (최대 1000건)
        String[] MgtKeyList = new String[]{"20230102-BOOT001", "20230102-BOOT002"};

        try {

            TaxinvoiceInfo[] taxinvoiceInfos =
                    taxinvoiceService.getInfos(CorpNum, mgtKeyType, MgtKeyList);

            m.addAttribute("TaxinvoiceInfos", taxinvoiceInfos);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceInfo";
    }

    @RequestMapping(value = "getDetailInfo", method = RequestMethod.GET)
    public String getDetailInfo(Model m) {
        /**
         * 세금계산서 1건의 상세정보를 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#GetDetailInfo
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {

            Taxinvoice taxinvoice = taxinvoiceService.getDetailInfo(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Taxinvoice", taxinvoice);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/Taxinvoice";
    }

    @RequestMapping(value = "getXML", method = RequestMethod.GET)
    public String getXML(Model m) {
        /**
         * 세금계산서 1건의 상세정보를 XML로 반환합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#GetXML
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {

            TaxinvoiceXML taxinvoiceXML = taxinvoiceService.getXML(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("TaxinvoiceXML", taxinvoiceXML);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceXML";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /**
         * 검색조건에 해당하는 세금계산서를 조회합니다. (조회기간 단위 : 최대 6개월) -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#Search
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 일자 유형 ("R" , "W" , "I" 중 택 1)
        // └ R = 등록일자 , W = 작성일자 , I = 발행일자
        String DType = "W";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20230102";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20230131";

        // 상태코드 배열 (2,3번째 자리에 와일드카드(*) 사용 가능)
        // - 미입력시 전체조회
        String[] State = {"3**", "6**"};

        // 문서유형 배열 ("N" , "M" 중 선택, 다중 선택 가능)
        // - N = 일반세금계산서 , M = 수정세금계산서
        // - 미입력시 전체조회
        String[] Type = {"N", "M"};

        // 과세형태 배열 ("T" , "N" , "Z" 중 선택, 다중 선택 가능)
        // - T = 과세 , N = 면세 , Z = 영세
        // - 미입력시 전체조회
        String[] TaxType = {"T", "N", "Z"};

        // 발행형태 배열 ("N" , "R" , "T" 중 선택, 다중 선택 가능)
        // - N = 정발행 , R = 역발행 , T = 위수탁발행
        // - 미입력시 전체조회
        String[] IssueType = {"N", "R", "T"};

        // 등록유형 배열 ("P" , "H" 중 선택, 다중 선택 가능)
        // - P = 팝빌 , H = 홈택스 또는 외부ASP
        // - 미입력시 전체조회
        String[] RegType = {"P", "H"};

        // 공급받는자 휴폐업상태 배열 ("N" , "0" , "1" , "2" , "3" , "4" 중 선택, 다중 선택 가능)
        // - N = 미확인 , 0 = 미등록 , 1 = 사업 , 2 = 폐업 , 3 = 휴업 , 4 = 확인실패
        // - 미입력시 전체조회
        String[] CloseDownState = {"N", "0", "1", "2", "3"};

        // 지연발행 여부 (null , true , false 중 택 1)
        // - null = 전체조회 , true = 지연발행 , false = 정상발행
        Boolean LateOnly = null;

        // 종사업장번호의 주체 ("S" , "B" , "T" 중 택 1)
        // └ S = 공급자 , B = 공급받는자 , T = 수탁자
        // - 미입력시 전체조회
        String TaxRegIDType = "";

        // 종사업장번호
        // 다수기재시 콤마(",")로 구분하여 구성 ex ) "0001,0002"
        // - 미입력시 전체조회
        String TaxRegID = "";

        // 종사업장번호 유무
        // - null = 전체 , 0 = 없음, 1 = 있음
        String TaxRegIDYN = null;

        // 거래처 상호 / 사업자번호 (사업자) / 주민등록번호 (개인) / "9999999999999" (외국인) 중 검색하고자 하는 정보 입력
        // - 사업자번호 / 주민등록번호는 하이픈('-')을 제외한 숫자만 입력
        // - 미입력시 전체조회
        String QString = "";

        // 문서번호 또는 국세청 승인번호 조회
        // - 미입력시 전체조회
        String MgtKey = "";

        // 페이지 번호
        int Page = 1;

        // 페이지당 목록개수
        int PerPage = 20;

        // 정렬방향, A-오름차순, D-내림차순
        String Order = "D";

        // 연동문서 여부 (null , "0" , "1" 중 택 1)
        // - null = 전체조회 , 0 = 일반문서 , 1 = 연동문서
        // 일반문서 - 세금계산서 작성 시 API가 아닌 팝빌 사이트를 통해 등록한 문서
        // 연동문서 - 세금계산서 작성 시 API를 통해 등록한 문서
        String InterOPYN = null;

        try {

            TISearchResult searchResult = taxinvoiceService.Search(CorpNum, mgtKeyType, DType,
                    SDate, EDate, State, Type, TaxType, IssueType, LateOnly, TaxRegIDType, TaxRegID,
                    TaxRegIDYN, QString, Page, PerPage, Order, InterOPYN, RegType, CloseDownState,
                    MgtKey, UserID);

            m.addAttribute("SearchResult", searchResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/SearchResult";
    }

    @RequestMapping(value = "getLogs", method = RequestMethod.GET)
    public String getLogs(Model m) {
        /**
         * 세금계산서의 상태에 대한 변경이력을 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#GetLogs
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {

            TaxinvoiceLog[] taxinvoiceLogs = taxinvoiceService.getLogs(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("TaxinvoiceLogs", taxinvoiceLogs);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceLog";
    }

    @RequestMapping(value = "getURL", method = RequestMethod.GET)
    public String getURL(Model m) {
        /**
         * 로그인 상태로 팝빌 사이트의 전자세금계산서 문서함 메뉴에 접근할 수 있는 페이지의 팝업 URL을 반환합니다. - 반환되는 URL은 보안 정책상 30초 동안
         * 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/info#GetURL
         */

        // TBOX : 임시문서함 , SBOX : 매출문서함 , PBOX : 매입문서함 ,
        // SWBOX : 매출발행 대기함 , PWBOX : 매입발행 대기함 , WRITE : 정발행 작성
        String TOGO = "SBOX";

        try {

            String url = taxinvoiceService.getURL(CorpNum, UserID, TOGO);

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
         * 세금계산서 1건의 상세 정보 페이지의 팝업 URL을 반환합니다. - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을
         * 통한 페이지 접근이 불가합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/view#GetPopUpURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            String url = taxinvoiceService.getPopUpURL(CorpNum, mgtKeyType, mgtKey, UserID);

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
         * 세금계산서 1건의 상세정보 페이지(사이트 상단, 좌측 메뉴 및 버튼 제외)의 팝업 URL을 반환합니다. - 반환되는 URL은 보안 정책상 30초 동안 유효하며,
         * 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/view#GetViewURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            String url = taxinvoiceService.getViewURL(CorpNum, mgtKeyType, mgtKey, UserID);

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
         * 세금계산서 1건을 인쇄하기 위한 페이지의 팝업 URL을 반환하며, 페이지내에서 인쇄 설정값을 "공급자" / "공급받는자" / "공급자+공급받는자"용 중 하나로
         * 지정할 수 있습니다. - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/view#GetPrintURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "202301028-BOOT001";

        try {

            String url = taxinvoiceService.getPrintURL(CorpNum, mgtKeyType, mgtKey, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getOldPrintURL", method = RequestMethod.GET)
    public String getOldPrintURL(Model m) {
        /**
         * 세금계산서 1건을 구버전 양식으로 인쇄하기 위한 페이지의 팝업 URL을 반환하며, 페이지내에서 인쇄 설정값을 "공급자" / "공급받는자" / "공급자+공급받는자"용 중 하나로 지정할 수 있습니다..
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/view#GetOldPrintURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            String url = taxinvoiceService.getOldPrintURL(CorpNum, mgtKeyType, mgtKey, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";

    }

    @RequestMapping(value = "getEPrintURL", method = RequestMethod.GET)
    public String getEPrintURL(Model m) {
        /**
         * "공급받는자" 용 세금계산서 1건을 인쇄하기 위한 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/view#GetEPrintURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            String url = taxinvoiceService.getEPrintURL(CorpNum, mgtKeyType, mgtKey, UserID);

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
         * 다수건의 세금계산서를 인쇄하기 위한 페이지의 팝업 URL을 반환합니다. (최대 100건) - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한
         * 후에는 해당 URL을 통한 페이지 접근이 불가합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/view#GetMassPrintURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 문서번호 배열, 최대 100건
        String[] MgtKeyList = new String[]{"20230102-BOOT001", "20230102-BOOT002"};

        try {

            String url =
                    taxinvoiceService.getMassPrintURL(CorpNum, mgtKeyType, MgtKeyList, UserID);

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
         * 전자세금계산서 안내메일의 상세보기 링크 URL을 반환합니다. - 함수 호출로 반환 받은 URL에는 유효시간이 없습니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/view#GetMailURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            String url = taxinvoiceService.getMailURL(CorpNum, mgtKeyType, mgtKey, UserID);

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
         * 전자세금계산서 PDF 파일을 다운 받을 수 있는 URL을 반환합니다. - 반환되는 URL은 보안정책상 30초의 유효시간을 갖으며, 유효시간 이후 호출시
         * 정상적으로 페이지가 호출되지 않습니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/view#GetPDFURL
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {

            String url = taxinvoiceService.getPDFURL(CorpNum, mgtKeyType, mgtKey, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getSealURL", method = RequestMethod.GET)
    public String getSealURL(Model m) {
        /**
         * 세금계산서에 첨부할 인감, 사업자등록증, 통장사본을 등록하는 페이지의 팝업 URL을 반환합니다. - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을
         * 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#GetSealURL
         */

        try {

            String url = taxinvoiceService.getSealURL(CorpNum, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "attachFile", method = RequestMethod.GET)
    public String attachFile(Model m) {
        /**
         * "임시저장" 상태의 세금계산서에 1개의 파일을 첨부합니다. (최대 5개) -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#AttachFile
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 첨부파일 표시명
        String displayName = "첨부파일.jpg";

        // 첨부할 파일의 InputStream. 예제는 resource에 테스트파일을 참조함.
        // FileInputStream으로 처리하는 것을 권함.
        InputStream stream =
                getClass().getClassLoader().getResourceAsStream("static/image/test.jpg");

        try {

            Response response =
                    taxinvoiceService.attachFile(CorpNum, mgtKeyType, mgtKey, displayName, stream);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }

        return "response";
    }

    @RequestMapping(value = "deleteFile", method = RequestMethod.GET)
    public String deleteFile(Model m) {
        /**
         * "임시저장" 상태의 세금계산서에 첨부된 1개의 파일을 삭제합니다.
         * - 파일을 식별하는 파일아이디는 첨부파일 목록 확인(GetFiles API) 함수의 응답항목 중 파일아이디(AttachedFile) 값을 통해 확인할 수 있습니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/etc#DeleteFile
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 파일아이디, getFiles()로 확인한 AttachedFile의 attachedFile 참조.
        String FileID = "";

        try {
            Response response = taxinvoiceService.deleteFile(CorpNum, mgtKeyType, mgtKey, FileID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getFiles", method = RequestMethod.GET)
    public String getFiles(Model m) {
        /**
         * 세금계산서에 첨부된 파일목록을 확인합니다.
         * - 응답항목 중 파일아이디(AttachedFile) 항목은 파일삭제(DeleteFile API) 함수 호출 시 이용할 수 있습니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/etc#GetFiles
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {

            AttachedFile[] attachedFiles = taxinvoiceService.getFiles(CorpNum, mgtKeyType, mgtKey);

            m.addAttribute("AttachedFiles", attachedFiles);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/AttachedFile";
    }

    @RequestMapping(value = "sendEmail", method = RequestMethod.GET)
    public String sendEmail(Model m) {
        /**
         * 세금계산서와 관련된 안내 메일을 재전송 합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#SendEmail
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 수신메일주소
        String receiverMail = "test@test.com";

        try {

            Response response =
                    taxinvoiceService.sendEmail(CorpNum, mgtKeyType, mgtKey, receiverMail);

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
        * 세금계산서와 관련된 안내 SMS(단문) 문자를 재전송하는 함수로, 팝빌 사이트 [문자·팩스] > [문자] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다.
        * - 메시지는 최대 90byte까지 입력 가능하고, 초과한 내용은 자동으로 삭제되어 전송합니다. (한글 최대 45자)
        * - 함수 호출시 포인트가 과금됩니다.
        * - https://developers.popbill.com/reference/taxinvoice/java/api/etc#SendSMS
        */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 발신번호
        String senderNum = "07043042991";

        // 수신번호
        String receiverNum = "010111222";

        // 메시지 내용, 90byte 초과시 길이가 조정되어 전송됨
        String contents = "문자 메시지 내용입니다. 세금계산서가 발행되었습니다.";

        try {

            Response response = taxinvoiceService.sendSMS(CorpNum, mgtKeyType, mgtKey, senderNum,
                    receiverNum, contents);

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
         * 세금계산서를 팩스로 전송하는 함수로, 팝빌 사이트 [문자·팩스] > [팩스] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다. - 함수 호출시 포인트가
         * 과금됩니다. - https://developers.popbill.com/reference/taxinvoice/java/api/etc#SendFAX
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 발신번호
        String senderNum = "07043042991";

        // 수신팩스번호
        String receiverNum = "070111222";

        try {

            Response response =
                    taxinvoiceService.sendFAX(CorpNum, mgtKeyType, mgtKey, senderNum, receiverNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "attachStatement", method = RequestMethod.GET)
    public String attachStatement(Model m) {
        /**
         * 팝빌 전자명세서 API를 통해 발행한 전자명세서를 세금계산서에 첨부합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#AttachStatement
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 첨부할 전자명세서 유형 코드 - 121(거래명세서), 122(청구서), 123(견적서), 124(발주서), 125(입금표), 126(영수증)
        int subItemCode = 121;

        // 첨부활 전자명세서 문서번호
        String subMgtKey = "20230102-BOOT002";

        try {

            Response response = taxinvoiceService.attachStatement(CorpNum, mgtKeyType, mgtKey,
                    subItemCode, subMgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "detachStatement", method = RequestMethod.GET)
    public String detachStatement(Model m) {
        /**
         * 세금계산서에 첨부된 전자명세서를 해제합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#DetachStatement
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 첨부해제할 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표],
        // [126 - 영수증]
        int subItemCode = 121;

        // 첨부해제할 전자명세서 문서번호
        String subMgtKey = "20230102-BOOT002";

        try {

            Response response = taxinvoiceService.detachStatement(CorpNum, mgtKeyType, mgtKey,
                    subItemCode, subMgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getEmailPublicKeys", method = RequestMethod.GET)
    public String getEmailPublicKeys(Model m) {
        /**
         * 전자세금계산서 유통사업자의 메일 목록을 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#GetEmailPublicKeys
         */

        try {

            EmailPublicKey[] emailPublicKeys = taxinvoiceService.getEmailPublicKeys(CorpNum);

            m.addAttribute("EmailPublicKeys", emailPublicKeys);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/EmailPublicKey";
    }

    @RequestMapping(value = "assignMgtKey", method = RequestMethod.GET)
    public String assignMgtKey(Model m) {
        /**
         * 팝빌 사이트를 통해 발행하여 문서번호가 부여되지 않은 세금계산서에 문서번호를 할당합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/etc#AssignMgtKey
         */

        // 세금계산서 유형 (SELL-매출, BUY-매입, TRUSTEE-위수탁)
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 팝빌번호, 문서 목록조회(Search) API의 반환항목중 ItemKey 참조
        String itemKey = "022021718272000001";

        // 할당할 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT007";

        try {

            Response response =
                    taxinvoiceService.assignMgtKey(CorpNum, mgtKeyType, itemKey, mgtKey);

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
         * 세금계산서 관련 메일 항목에 대한 발송설정을 확인합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/etc#ListEmailConfig
         */
        Map<String, Boolean> emailSendConfigs = new HashMap<String, Boolean>();

        try {

            EmailSendConfig[] Configs = taxinvoiceService.listEmailConfig(CorpNum);

            for (EmailSendConfig emailSendConfig : Configs) {
                emailSendConfigs.put(emailSendConfig.getEmailType(), emailSendConfig.getSendYN());
            }

            m.addAttribute("EmailSendConfigs", emailSendConfigs);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/EmailSendConfig";
    }

    @RequestMapping(value = "updateEmailConfig", method = RequestMethod.GET)
    public String updateEmailConfig(Model m) {
        /**
         * 세금계산서 관련 메일 항목에 대한 발송설정을 수정합니다.
         * - https://developers.popbill.com/reference/taxinvoice/java/api/etc#UpdateEmailConfig
         *
         * 메일전송유형
         * [정발행]
         * - TAX_ISSUE : 공급받는자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다.
         * - TAX_ISSUE_INVOICER : 공급자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다.
         * - TAX_CHECK : 공급자에게 전자세금계산서가 수신확인 되었음을 알려주는 메일입니다.
         * - TAX_CANCEL_ISSUE : 공급받는자에게 전자세금계산서가 발행취소 되었음을 알려주는 메일입니다.
         *
         * [역발행] - TAX_REQUEST : 공급자에게 세금계산서를 전자서명 하여 발행을 요청하는 메일입니다. - TAX_CANCEL_REQUEST : 공급받는자에게
         * 세금계산서가 취소 되었음을 알려주는 메일입니다. - TAX_REFUSE : 공급받는자에게 세금계산서가 거부 되었음을 알려주는 메일입니다.
         *
         * [위수탁발행] - TAX_TRUST_ISSUE : 공급받는자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다. -
         * TAX_TRUST_ISSUE_TRUSTEE : 수탁자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다. - TAX_TRUST_ISSUE_INVOICER :
         * 공급자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다. - TAX_TRUST_CANCEL_ISSUE : 공급받는자에게 전자세금계산서가 발행취소 되었음을
         * 알려주는 메일입니다. - TAX_TRUST_CANCEL_ISSUE_INVOICER : 공급자에게 전자세금계산서가 발행취소 되었음을 알려주는 메일입니다.
         *
         * [처리결과] - TAX_CLOSEDOWN : 거래처의 휴폐업 여부를 확인하여 안내하는 메일입니다. - TAX_NTSFAIL_INVOICER : 전자세금계산서
         * 국세청 전송실패를 안내하는 메일입니다.
         *
         * [정기발송] - ETC_CERT_EXPIRATION : 공급자/수탁자가 팝빌에 등록한 인증서의 만료일이 45/30일 남았을 경우 안내하는 메일입니다.
         */

        // 메일 전송 유형
        String emailType = "TAX_ISSUE";

        // 전송 여부 (true = 전송, false = 미전송)
        Boolean sendYN = true;

        try {

            Response response = taxinvoiceService.updateEmailConfig(CorpNum, emailType, sendYN);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getSendToNTSConfig", method = RequestMethod.GET)
    public String getSendToNTSConfig(Model m) {
        /**
         * 연동회원의 국세청 전송 옵션 설정 상태를 확인합니다. - 국세청 전송 옵션 설정은 팝빌 사이트 [전자세금계산서] > [환경설정] > [세금계산서 관리] 메뉴에서
         * 설정할 수 있으며, API로 설정은 불가능 합니다. - 팝빌 국세청 전송 정책
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/policy-of-send-to-nts]
         * - https://developers.popbill.com/reference/taxinvoice/java/api/etc#GetSendToNTSConfig
         */
        try {

            boolean ntsConfig = taxinvoiceService.getSendToNTSConfig(CorpNum);

            m.addAttribute("NTSConfig", ntsConfig);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "/Taxinvoice/getSendToNTSConfig";
    }

    @RequestMapping(value = "getTaxCertURL", method = RequestMethod.GET)
    public String getTaxCertURL(Model m) {
        /**
         * 전자세금계산서 발행에 필요한 인증서를 팝빌 인증서버에 등록하기 위한 페이지의 팝업 URL을 반환합니다. - 반환되는 URL은 보안 정책상 30초 동안 유효하며,
         * 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. - 인증서 갱신/재발급/비밀번호 변경한 경우, 변경된 인증서를 팝빌 인증서버에 재등록
         * 해야합니다. - https://developers.popbill.com/reference/taxinvoice/java/api/cert#GetTaxCertURL
         */

        try {

            String url = taxinvoiceService.getTaxCertURL(CorpNum, UserID);

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
         * 팝빌 인증서버에 등록된 인증서의 만료일을 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/cert#GetCertificateExpireDate
         */

        try {

            Date expireDate = taxinvoiceService.getCertificateExpireDate(CorpNum);

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
         * 팝빌 인증서버에 등록된 인증서의 유효성을 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/cert#CheckCertValidation
         */

        try {

            Response response = taxinvoiceService.checkCertValidation(CorpNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getTaxCertInfo", method = RequestMethod.GET)
    public String getTaxCertInfo(Model m) {
        /**
         * 팝빌 인증서버에 등록된 공동인증서의 정보를 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/cert#GetTaxCertInfo
         */

        try {
            TaxinvoiceCertificate taxinvoiceCertificate = taxinvoiceService.getTaxCertInfo(CorpNum);

            m.addAttribute("TaxinvoiceCertificate", taxinvoiceCertificate);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Taxinvoice/TaxinvoiceCertificate";
    }

    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /**
         * 세금계산서 발행시 과금되는 포인트 단가를 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/point#GetUnitCost
         */

        try {

            float unitCost = taxinvoiceService.getUnitCost(CorpNum);

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
         * 팝빌 전자세금계산서 API 서비스 과금정보를 확인합니다. -
         * https://developers.popbill.com/reference/taxinvoice/java/api/point#GetChargeInfo
         */

        try {

            ChargeInfo chrgInfo = taxinvoiceService.getChargeInfo(CorpNum);

            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

    @RequestMapping(value = "modifyTaxinvoice01minus", method = RequestMethod.GET)
    public String modifyTaxinvoice01minus(Model m) {
        /**
         * 기재사항 착오정정 수정세금계산서를 발행합니다.
         * - '필요적 기재사항'이나 '임의적 기재사항' 등을 착오 또는 착오 외의 사유로 잘못 작성하거나, 세율을 잘못 적용하여 신고한 경우 이용하는 수정사유 입니다.
         * - 기재사항 착오정정 수정세금계산서는 총 2장(취소분/수정분) 발급해야 합니다.
         * - https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice
         */

        /**
         **************** 기재사항 착오정정 수정세금계산서 예시 (취소분) ****************
         * 작성일자 1월 2일 공급가액 200,000원으로 매출 세금계산서를 발급해야 하는데, 공급가액 100,000원으로 잘못 발급 한 경우
         * 원본 전자세금계산서와 동일한 내용의 부(-) 세금계산서 발행
         */

        // 원본 세금계산서를 취소할 세금계산서 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        // 원본 세금계산서 작성 일자 기재
        taxinvoice.setWriteDate("20230102");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("-100000");

        // 세액 합계
        taxinvoice.setTaxTotal("-10000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("-110000");

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-modify-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-1111-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/


        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 1);

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum("20230706-original-TI00001");

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail taxinvoiceDetail = new TaxinvoiceDetail();

        taxinvoiceDetail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        taxinvoiceDetail.setPurchaseDT("20230102"); // 거래일자
        taxinvoiceDetail.setItemName("품목명"); // 품목명
        taxinvoiceDetail.setSpec("규격"); // 규격
        taxinvoiceDetail.setQty("1"); // 수량
        taxinvoiceDetail.setUnitCost("-50000"); // 단가
        taxinvoiceDetail.setSupplyCost("-50000"); // 공급가액
        taxinvoiceDetail.setTax("-5000"); // 세액
        taxinvoiceDetail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(taxinvoiceDetail);

        taxinvoiceDetail = new TaxinvoiceDetail();

        taxinvoiceDetail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        taxinvoiceDetail.setPurchaseDT("20230102"); // 거래일자
        taxinvoiceDetail.setItemName("품목명2"); // 품목명
        taxinvoiceDetail.setSpec("규격"); // 규격
        taxinvoiceDetail.setQty("1"); // 수량
        taxinvoiceDetail.setUnitCost("-50000"); // 단가
        taxinvoiceDetail.setSupplyCost("-50000"); // 공급가액
        taxinvoiceDetail.setTax("-5000"); // 세액
        taxinvoiceDetail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(taxinvoiceDetail);

        /**********************************************************************
         * 추가담당자 정보 - 세금계산서 발행 안내 메일을 수신받을 공급받는자 담당자가 다수인 경우 - 담당자 정보를 추가하여 발행 안내메일을 다수에게 전송할 수
         * 있습니다. (최대 5명)
         *********************************************************************/

        // taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        // TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        // addContact.setSerialNum(1);
        // addContact.setContactName("추가 담당자 성명");
        // addContact.setEmail("test2@test.com");

        // taxinvoice.getAddContactList().add(addContact);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        try {

            // 취소분 세금계산서 발행
            IssueResponse response = taxinvoiceService.registIssue(CorpNum,
                    taxinvoice, Memo, ForceIssue);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice01plus", method = RequestMethod.GET)
    public String modifyTaxinvoice01plus(Model m) {
        /**
         * 기재사항 착오정정 수정세금계산서를 발행합니다.
         * - '필요적 기재사항'이나 '임의적 기재사항' 등을 착오 또는 착오 외의 사유로 잘못 작성하거나, 세율을 잘못 적용하여 신고한 경우 이용하는 수정사유 입니다
         * - 기재사항 착오정정 수정세금계산서는 총 2장(취소분/수정분) 발급해야 합니다.
         * - https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice
         */

        /**
         **************** 기재사항 착오정정 수정세금계산서 예시 (수정분) ****************
         * 작성일자 1월 2일 공급가액 200,000원으로 매출 세금계산서를 발급해야 하는데, 공급가액 100,000원으로 잘못 발급 한 경우
         * 수정사항을 반영한 정(+) 세금계산서를 발행
         */

        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        // 원본 전자세금계산서 작성일자 또는 변경을 원하는 작성일자
        taxinvoice.setWriteDate("20230102");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("200000");

        // 세액 합계
        taxinvoice.setTaxTotal("20000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("220000");

        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 1);

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/


        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;
        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, Memo, ForceIssue);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice02", method = RequestMethod.GET)
    public String modifyTaxinvoice02(Model m) {
        /**
         * 공급가액 변동에 의한 수정세금계산서 발행
         * - 일부 금액의 계약의 해지 등을 포함하여 공급가액의 증가 또는 감소가 발생한 경우 이용하는 수정사유 입니다.
         * - 증가 : 원본 전자세금계산서 공급가액에서 증가한 금액만큼만 수정분 정(+) 세금계산서 발행
         * - 감소 : 원본 전자세금계산서 공급가액에서 감소한 금액만큼만 수정분 부(-) 세금계산서 발행
         * - ※ 원본 전자세금계산서 공급가액 + 수정세금계산서 공급가액(+/-) = 최종 공급가액
         * - 수정세금계산서 가이드: [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         */

        /**
         **************** 공급가액 변동에 의한 수정세금계산서 예시 ****************
         * 작성일자 2월 7일 공급가액 30,000원으로 매출 세금계산서를 발급해야 하는데, 공급가액 50,000원으로 잘못  발급한 경우
         * 원본 공급가액의 50,000원에서 차감되어야 하는 금액이 -20,000원의 수정세금계산서 발행
         */

        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        // 공급가액 변동이 발생한 날
        taxinvoice.setWriteDate("20230207");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("-20000");

        // 세액 합계
        taxinvoice.setTaxTotal("-2000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("-22000");

        // 비고
        // 공급가액 변동으로 인한 수정 세금계산서 작성 시, 원본 세금계산서 작성일자 기재 필수
        taxinvoice.setRemark1("20230207");

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 2);

        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("-50000"); // 단가
        detail.setSupplyCost("-50000"); // 공급가액
        detail.setTax("-5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("-50000"); // 단가
        detail.setSupplyCost("-50000"); // 공급가액
        detail.setTax("-5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, Memo, ForceIssue);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice03", method = RequestMethod.GET)
    public String modifyTaxinvoice03(Model m) {
        /**
         * 환입에 의한 수정세금계산서 발행
         * - 당초 공급한 재화가 환입(반품)되는 경우 이용하는 수정사유 입니다.
         * - 환입(반품)된 금액 만큼만 수정분 부(-) 세금계산서 발행
         * - 수정세금계산서 가이드: [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         */

        /**
         **************** 환입에 의한 수정세금계산서 예시 ****************
         *  2월 8일 공급가액 30,000원의 세금계산서를 발급했으나, 2월 12일에 10,000원에 해당되는 물품이 환입(반품)된 경우
         *  2월 12일 작성일자로 환입(반품) 금액 10,000원에 대해 환입 사유로 세금계산서를 발행
         */
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        // 환입이 발생한 날 기재
        taxinvoice.setWriteDate("20230212");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("-10000");

        // 세액 합계
        taxinvoice.setTaxTotal("-1000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("-11000");

        // 비고
        // - 환입에 의한 수정세금계산서 작성의 경우, 원본 세금계산서의 작성일자 기재 필수
        taxinvoice.setRemark1("20230208");

        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 3);

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/


        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, Memo, ForceIssue);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice04", method = RequestMethod.GET)
    public String modifyTaxinvoice04(Model m) {
        /**
         * 계약의 해제에 의한 수정세금계산서 발행
         * - 재화 또는 용역/서비스가 공급되지 아니하였거나 계약이 해제된 경우 이용하는 수정사유 입니다.
         * - 원본 전자세금계산서와 동일한 내용의 부(-) 세금계산서 발행
         * - 수정세금계산서 가이드: [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         */

        /**
         **************** 계약의 해제에 의한 수정세금계산서 예시 ****************
         * 2월 13일 공급가액 30,000원의 세금계산서를 발급했으나, 2월 15일에 전체 계약이 해제(취소)된 경우
         * 계약이 취소된 2월 15일을 작성일자로 계약의 해제 사유의 수정세금계산서를 발행
         */
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20230215");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("-30000");

        // 세액 합계
        taxinvoice.setTaxTotal("-3000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("-33000");

        // 비고
        // 계약의 해제에 의한 수정세금계산서 발행의 경우, 원본 세금계산서의 작성 일자 기재
        taxinvoice.setRemark1("20230213");

        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 4);

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/


        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);
        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, false);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice05minus", method = RequestMethod.GET)
    public String modifyTaxinvoice05minus(Model m) {
        /**
         * 내국신용장 사후개설에 의한 수정세금계산서 발행
         * - 재화 또는 서비스/용역을 공급한 시기가 속하는 과세기간 종료(1/1~6/30 또는 7/1~12/31) 다음달(7월 또는 1월) 25일 이내에 내국신용장이 개설되었거나 구매확인서가 발급된 경우 이용하는 수정사유 입니다.
         * - 취소분 : 내국신용장이 개설된 품목에 대한 부(-) 세금계산서 발행
         * - 수정분 : 내국신용장이 개설된 품목에 대한 정(+) 영세율 세금계산서 발행
         * - 수정세금계산서 가이드: [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         */

        /**
         **************** 내국신용장 사후개설에 의한 수정세금계산서 예시 (취소분) ****************
         * 3월 13일 공급가액 3,000,000원의 전자세금계산서를 발급한 후, 4월 12일에 일부 품목인 공급가액 1,000,000원에 대해 내국신용장이 개설된 경우
         * 내국 신용장 사후개설된 1,000,000원에 대한 3월 13일 작성일자의 영세율 세금계산서 작성
         */

        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        // 원본 세금계산서의 작성일자 기재
        taxinvoice.setWriteDate("20230313");

        // 과세형태, [과세, 영세, 면세] 중 기재
        // 부 세금계산서의 경우 과세
        taxinvoice.setTaxType("과세");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("-1000000");

        // 세액 합계
        taxinvoice.setTaxTotal("-100000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("-1100000");

        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 5);

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");


        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/


        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;
        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, Memo, ForceIssue);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice05plus", method = RequestMethod.GET)
    public String modifyTaxinvoice05plus(Model m) {
        /**
         * 내국신용장 사후개설에 의한 수정세금계산서 발행
         * - 재화 또는 서비스/용역을 공급한 시기가 속하는 과세기간 종료(1/1~6/30 또는 7/1~12/31) 다음달(7월 또는 1월) 25일 이내에 내국신용장이 개설되었거나 구매확인서가 발급된 경우 이용하는 수정사유 입니다.
         * - 취소분 : 내국신용장이 개설된 품목에 대한 부(-) 세금계산서 발행
         * - 수정분 : 내국신용장이 개설된 품목에 대한 정(+) 영세율 세금계산서 발행
         * - 수정세금계산서 가이드: [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         */

        /**
         **************** 내국신용장 사후개설에 의한 수정세금계산서 예시 (수정분) ****************
         * 3월 13일 공급가액 3,000,000원의 전자세금계산서를 발급한 후, 4월 12일에 일부 품목인 공급가액 1,000,000원에 대해 내국신용장이 개설된 경우
         * 내국 신용장 사후개설된 1,000,000원에 대한 3월 13일 작성일자의 영세율 세금계산서 작성
         */

        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        //  원본 세금계산서의 작성 일자
        taxinvoice.setWriteDate("20230313");

        // 공급가액 합계
        taxinvoice.setSupplyCostTotal("1000000");

        // 세액 합계
        taxinvoice.setTaxTotal("100000");

        // 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("1100000");

        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode((short) 5);

        // 비고
        // - 내국신용장 사후개설에 의한 수정세금계산서 발행 시, 비고란에 내국신용장 개설일자 기재
        taxinvoice.setRemark1("20230412");

        // 과세형태, [과세, 영세, 면세] 중 기재
        // 내국신용장 개설 품목에 대해 영세율 세금계산서 작성
        taxinvoice.setTaxType("영세");



        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");


        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/


        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/


        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;
        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, Memo, ForceIssue);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }

    @RequestMapping(value = "modifyTaxinvoice06", method = RequestMethod.GET)
    public String modifyTaxinvoice06(Model m) {
        /**
         * 착오에 의한 이중발급에 의한 수정세금계산서 발행
         * - 1건의 거래에 대한 단순 착오로 인해 2건 이상의 전자세금계산서를 발행하거나, 과세형태(과세/영세율↔면세) 착오로 잘못 발급된 경우 이용하는 수정사유 입니다.
         * - 원본 전자세금계산서와 동일한 내용의 수정분 부(-) 세금계산서 발행
         * - 수정세금계산서 가이드: [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         */

        /**
         **************** 착오에 의한 이중발급에 의한 수정세금계산서 예시 ****************
         * 작성일자 2월 16일자에 물건을 납품하고 공급가액 80,000원의 세금계산서를 발급했습니다
         * 그런데 회계담당자의 실수로 동일한 세금계산서를 중복으로 발행 한 경우
         *  [착오에의한 이중발급] 사유로 작성일자 2월 16일, 공급가액 마이너스(-)80,000원의 수정세금계산서를 발급
         */
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        // 착오에 의한 이중발급 사유로 수정세금계산서 작성 시, 원본 전자세금계산서 작성일자 기재
        taxinvoice.setWriteDate("20230701");

        // 공급가액 합계
        // 원본 세금계산서와 동일한 내용의 수정분 부(-) 기재
        taxinvoice.setSupplyCostTotal("-80000");

        // 세액 합계
        // 원본 세금계산서와 동일한 내용의 수정분 부(-) 기재
        taxinvoice.setTaxTotal("-8000");

        // 합계금액, 공급가액 + 세액
        // 원본 세금계산서와 동일한 내용의 수정분 부(-) 기재
        taxinvoice.setTotalAmount("-88000");

        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        // 착오에 의한 이중발급 사유로 수정세금계산서 작성 시, 수정사유코드 6 기재
        taxinvoice.setModifyCode((short) 6);

        // 과금방향, [정과금, 역과금] 중 선택기재
        // └ 정과금 = 공급자 과금 , 역과금 = 공급받는자 과금
        // -"역과금"은 역발행 세금계산서 발행 시에만 이용가능
        taxinvoice.setChargeDirection("정과금");

        // 발행형태, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구, 없음] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /**********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(CorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20230102-BOOT001");

        // 공급자 대표자 성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 종목");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자 성명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급받는자 (주)담당자 휴대폰번호 {invoiceeHP1} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /**********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호
        // - {invoiceeType}이 "사업자" 인 경우, 사업자번호 (하이픈 ('-') 제외 10자리)
        // - {invoiceeType}이 "개인" 인 경우, 주민등록번호 (하이픈 ('-') 제외 13자리)
        // - {invoiceeType}이 "외국인" 인 경우, "9999999999999" (하이픈 ('-') 제외 13자리)
        taxinvoice.setInvoiceeCorpNum("8888888888");

        // 공급받는자 종사업장 식별번호, 필요시 숫자4자리 기재
        taxinvoice.setInvoiceeTaxRegID("");

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 1~24자리 (숫자, 영문, '-', '_') 를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자 성명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자 성명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행 안내 문자 전송여부 (true / false 중 택 1)
        // └ true = 전송 , false = 미전송
        // └ 공급자 담당자 휴대폰번호 {invoicerHP} 값으로 문자 전송
        // - 전송 시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /**********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // 일련번호
        taxinvoice.setSerialNum("123");

        // 현금
        taxinvoice.setCash("");

        // 수표
        taxinvoice.setChkBill("");

        // 어음
        taxinvoice.setNote("");

        // 외상미수금
        taxinvoice.setCredit("");

        // 비고
        // {invoiceeType}이 "외국인" 이면 remark1 필수
        // - 외국인 등록번호 또는 여권번호 입력
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");

        // 책번호 '권' 항목, 최대값 32767
        taxinvoice.setKwon((short) 1);

        // 책번호 '호' 항목, 최대값 32767
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        taxinvoice.setBankBookYN(false);

        /**********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 작성방법 안내
         * [https://developers.popbill.com/guide/taxinvoice/java/introduction/modified-taxinvoice]
         *********************************************************************/


        // 수정세금계산서 작성시 원본세금계산서 국세청 승인번호 기재
        taxinvoice.setOrgNTSConfirmNum(null);

        /**********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고"); // 비고

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2"); // 비고

        taxinvoice.getDetailList().add(detail);

        // 즉시발행 메모
        String Memo = "수정세금계산서 발행 메모";

        // 지연발행 강제여부  (true / false 중 택 1)
        // └ true = 가능 , false = 불가능
        // - 미입력 시 기본값 false 처리
        // - 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // - 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        //   true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;
        try {
            IssueResponse response = taxinvoiceService.registIssue(CorpNum, taxinvoice, Memo, ForceIssue);
            m.addAttribute("Response", response);
        } catch (PopbillException pe) {
            m.addAttribute("Exception", pe);
            return "exception";
        }

        return "issueResponse";
    }
}
