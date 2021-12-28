package com.popbill.example.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.AttachedFile;
import com.popbill.api.BulkResponse;
import com.popbill.api.ChargeInfo;
import com.popbill.api.EmailSendConfig;
import com.popbill.api.IssueResponse;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.TaxinvoiceService;
import com.popbill.api.taxinvoice.BulkTaxinvoiceResult;
import com.popbill.api.taxinvoice.EmailPublicKey;
import com.popbill.api.taxinvoice.MgtKeyType;
import com.popbill.api.taxinvoice.TISearchResult;
import com.popbill.api.taxinvoice.Taxinvoice;
import com.popbill.api.taxinvoice.TaxinvoiceAddContact;
import com.popbill.api.taxinvoice.TaxinvoiceDetail;
import com.popbill.api.taxinvoice.TaxinvoiceInfo;
import com.popbill.api.taxinvoice.TaxinvoiceLog;

@Controller
@RequestMapping(value = "TaxinvoiceService")
public class TaxinvoiceServiceController {
    
    @Autowired
    private TaxinvoiceService taxinvoiceService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Taxinvoice/index";
    }

    @RequestMapping(value = "checkMgtKeyInUse", method = RequestMethod.GET)
    public String checkMgtKeyInUse(Model m) {
        /*
         * 파트너가 세금계산서 관리 목적으로 할당하는 문서번호의 사용여부를 확인합니다.
         * - 문서번호는 최대 24자리 영문 대소문자, 숫자, 특수문자('-','_')로 구성 합니다.
         * - https://docs.popbill.com/taxinvoice/java/api#CheckMgtKeyInUse
         */
        
        MgtKeyType keyType = MgtKeyType.SELL;

        String mgtKey = "20211118-001-96";

        String isUseStr;

        try {

            boolean IsUse = taxinvoiceService.checkMgtKeyInUse(testCorpNum, keyType, mgtKey);

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
        /*
         * 작성된 세금계산서 데이터를 팝빌에 저장과 동시에 발행(전자서명)하여 "발행완료" 상태로 처리합니다. 
         * - 세금계산서 국세청 전송 정책 : https://docs.popbill.com/taxinvoice/ntsSendPolicy?lang=java 
         * - https://docs.popbill.com/taxinvoice/java/api#RegistIssue
         */

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20211122");

        // 과금방향, [정과금, 역과금] 중 선택기재, "역과금"은 역발행세금계산서 발행에만 가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /*********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(testCorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20211124-001");

        // 공급자 대표자성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 업종");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /*********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호, '-' 제외 10자리
        taxinvoice.setInvoiceeCorpNum("8888888888");

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

        // 공급받는자 담당자명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행시 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /*********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // [필수] 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // [필수] 세액 합계
        taxinvoice.setTaxTotal("10000");

        // [필수] 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 기재 상 일련번호
        taxinvoice.setSerialNum("123");

        // 기재 상 현금
        taxinvoice.setCash("");

        // 기재 상 수표
        taxinvoice.setChkBill("");

        // 기재 상 어음
        taxinvoice.setNote("");

        // 기재 상 외상미수금
        taxinvoice.setCredit("");

        // 기재 상 비고
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");
        taxinvoice.setKwon((short) 1);
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부
        taxinvoice.setBankBookYN(false);

        /*********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) 
         * - 수정세금계산서 관련 정보는 연동매뉴얼 또는 개발가이드 링크 참조 & 
         * - [참고] 수정세금계산서 작성방법 안내 [https://docs.popbill.com/taxinvoice/modify?lang=java]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 원본세금계산서 국세청승인번호
        taxinvoice.setOrgNTSConfirmNum("");

        /*********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211122"); // 거래일자
        detail.setItemName("품목명");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고");

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211122"); // 거래일자
        detail.setItemName("품목명2");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2");

        taxinvoice.getDetailList().add(detail);

        /*********************************************************************
         * 추가담당자 정보
         *********************************************************************/

//        taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());
//
//        TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();
//
//        addContact.setSerialNum(1);
//        addContact.setContactName("추가 담당자명");
//        addContact.setEmail("test2@test.com");
//
//        taxinvoice.getAddContactList().add(addContact);

        // 거래명세서 동시작성여부
        Boolean WriteSpecification = false;

        // 거래명세서 문서번호
        String DealInvoiceKey = null;

        // 즉시발행 메모
        String Memo = "즉시발행 메모";

        // 지연발행 강제여부
        // 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        // true로 선언하여 발행(Issue API)를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        try {

            IssueResponse response = taxinvoiceService.registIssue(testCorpNum, taxinvoice, WriteSpecification, Memo,
                    ForceIssue, DealInvoiceKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/issueResponse";
    }

    @RequestMapping(value = "bulkSubmit", method = RequestMethod.GET)
    public String bulkSubmit(Model m) {

        /*
         * 최대 100건의 세금계산서 발행을 한번의 요청으로 접수합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#BulkSubmit
         */

        // 제출아이디, 대량 발행 접수를 구별하는 식별키
        // └ 최대 36자리 영문, 숫자, '-' 조합으로 구성
        String SubmitID = "20211122-001";

        // 지연발행 강제여부
        // 발행마감일이 지난 세금계산서를 발행하는 경우, 가산세가 부과될 수 있습니다.
        // 가산세가 부과되더라도 발행을 해야하는 경우에는 forceIssue의 값을
        // true로 선언하여 API를 호출하시면 됩니다.
        Boolean ForceIssue = false;

        List<Taxinvoice> bulkTx = new ArrayList<Taxinvoice>();

        for (int i = 0; i < 100; i++) {

            // 세금계산서 정보 객체
            Taxinvoice taxinvoice = new Taxinvoice();

            // 작성일자, 날짜형식(yyyyMMdd)
            taxinvoice.setWriteDate("20211122");

            // 과금방향, [정과금, 역과금] 중 선택기재, "역과금"은 역발행세금계산서 발행에만 가능
            taxinvoice.setChargeDirection("정과금");

            // 발행유형, [정발행, 역발행, 위수탁] 중 기재
            taxinvoice.setIssueType("정발행");

            // [영수, 청구] 중 기재
            taxinvoice.setPurposeType("영수");

            // 과세형태, [과세, 영세, 면세] 중 기재
            taxinvoice.setTaxType("과세");

            /*********************************************************************
             * 공급자 정보
             *********************************************************************/

            // 공급자 사업자번호
            taxinvoice.setInvoicerCorpNum(testCorpNum);

            // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
            taxinvoice.setInvoicerTaxRegID("");

            // 공급자 상호
            taxinvoice.setInvoicerCorpName("공급자 상호");

            // 공급자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
            taxinvoice.setInvoicerMgtKey(SubmitID + "-" + String.valueOf(i + 1));

            // 공급자 대표자성명
            taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

            // 공급자 주소
            taxinvoice.setInvoicerAddr("공급자 주소");

            // 공급자 종목
            taxinvoice.setInvoicerBizClass("공급자 업종");

            // 공급자 업태
            taxinvoice.setInvoicerBizType("공급자 업태,업태2");

            // 공급자 담당자 성명
            taxinvoice.setInvoicerContactName("공급자 담당자명");

            // 공급자 담당자 메일주소
            taxinvoice.setInvoicerEmail("test@test.com");

            // 공급자 담당자 연락처
            taxinvoice.setInvoicerTEL("070-7070-0707");

            // 공급자 담당자 휴대폰번호
            taxinvoice.setInvoicerHP("010-000-2222");

            // 발행 안내문자메시지 전송여부
            // - 전송시 포인트 차감되며, 전송실패시 환불처리
            taxinvoice.setInvoicerSMSSendYN(false);

            /*********************************************************************
             * 공급받는자 정보
             *********************************************************************/

            // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
            taxinvoice.setInvoiceeType("사업자");

            // 공급받는자 사업자번호, '-' 제외 10자리
            taxinvoice.setInvoiceeCorpNum("8888888888");

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

            // 공급받는자 담당자명
            taxinvoice.setInvoiceeContactName1("공급받는자 담당자명");

            // 공급받는자 담당자 메일주소
            // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
            // 실제 거래처의 메일주소가 기재되지 않도록 주의
            taxinvoice.setInvoiceeEmail1("");

            // 공급받는자 담당자 연락처
            taxinvoice.setInvoiceeTEL1("070-111-222");

            // 공급받는자 담당자 휴대폰번호
            taxinvoice.setInvoiceeHP1("010-111-222");

            // 역발행시 안내문자메시지 전송여부
            // - 전송시 포인트 차감되며, 전송실패시 환불처리
            taxinvoice.setInvoiceeSMSSendYN(false);

            /*********************************************************************
             * 세금계산서 기재정보
             *********************************************************************/

            // [필수] 공급가액 합계
            taxinvoice.setSupplyCostTotal("100000");

            // [필수] 세액 합계
            taxinvoice.setTaxTotal("10000");

            // [필수] 합계금액, 공급가액 + 세액
            taxinvoice.setTotalAmount("110000");

            // 기재 상 일련번호
            taxinvoice.setSerialNum("123");

            // 기재 상 현금
            taxinvoice.setCash("");

            // 기재 상 수표
            taxinvoice.setChkBill("");

            // 기재 상 어음
            taxinvoice.setNote("");

            // 기재 상 외상미수금
            taxinvoice.setCredit("");

            // 기재 상 비고
            taxinvoice.setRemark1("비고1");
            taxinvoice.setRemark2("비고2");
            taxinvoice.setRemark3("비고3");
            taxinvoice.setKwon((short) 1);
            taxinvoice.setHo((short) 1);

            // 사업자등록증 이미지 첨부여부
            taxinvoice.setBusinessLicenseYN(false);

            // 통장사본 이미지 첨부여부
            taxinvoice.setBankBookYN(false);

            /*********************************************************************
             * 수정세금계산서 정보 (수정세금계산서 작성시 기재) - 수정세금계산서 관련 정보는 연동매뉴얼 또는 개발가이드 링크 참조 & - [참고] 수정세금계산서 작성방법 안내 [https://docs.popbill.com/taxinvoice/modify?lang=java]
             *********************************************************************/
            // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
            taxinvoice.setModifyCode(null);

            // 원본세금계산서 국세청승인번호
            taxinvoice.setOrgNTSConfirmNum("");

            /*********************************************************************
             * 상세항목(품목) 정보
             *********************************************************************/

            taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

            // 상세항목 객체
            TaxinvoiceDetail detail = new TaxinvoiceDetail();

            detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
            detail.setPurchaseDT("20211122"); // 거래일자
            detail.setItemName("품목명");
            detail.setSpec("규격");
            detail.setQty("1"); // 수량
            detail.setUnitCost("50000"); // 단가
            detail.setSupplyCost("50000"); // 공급가액
            detail.setTax("5000"); // 세액
            detail.setRemark("품목비고");

            taxinvoice.getDetailList().add(detail);

            detail = new TaxinvoiceDetail();

            detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
            detail.setPurchaseDT("20211122"); // 거래일자
            detail.setItemName("품목명2");
            detail.setSpec("규격");
            detail.setQty("1"); // 수량
            detail.setUnitCost("50000"); // 단가
            detail.setSupplyCost("50000"); // 공급가액
            detail.setTax("5000"); // 세액
            detail.setRemark("품목비고2");

            taxinvoice.getDetailList().add(detail);

            /*********************************************************************
             * 추가담당자 정보
             *********************************************************************/

            taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

            TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

            addContact.setSerialNum(1);
            addContact.setContactName("추가 담당자명");

            // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
            // 실제 거래처의 메일주소가 기재되지 않도록 주의
            addContact.setEmail("test2@test.com");

            taxinvoice.getAddContactList().add(addContact);

            bulkTx.add(taxinvoice);
        }

        try {

            BulkResponse response = taxinvoiceService.bulkSubmit(testCorpNum, SubmitID, bulkTx, ForceIssue);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/bulkSubmitResponse";
    }

    @RequestMapping(value = "getBulkResult", method = RequestMethod.GET)
    public String getBulkResult(Model m) {
        /*
         * 접수시 기재한 SubmitID를 사용하여 세금계산서 접수결과를 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetBulkResult
         */

        // 대량 발행 접수시 기재한 제출아이디
        String SubmitID = "20211122-001";

        try {

            BulkTaxinvoiceResult bulkResult = taxinvoiceService.getBulkResult(testCorpNum, SubmitID);

            m.addAttribute("BulkResult", bulkResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/GetBulkResult";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register(Model m) {
        /*
         * 작성된 세금계산서 데이터를 팝빌에 저장합니다. 
         * - "임시저장" 상태의 세금계산서는 발행(Issue)함수를 호출하여 "발행완료" 처리한 경우에만 국세청으로 전송됩니다. 
         * - 정발행시 임시저장(Register)과 발행(Issue)을 한번의 호출로 처리하는 즉시발행(RegistIssue API) 프로세스 연동을 권장합니다. 
         * - 역발행시 임시저장(Register)과 역발행요청(Request)을 한번의 호출로 처리하는 즉시요청(RegistRequest API) 프로세스 연동을 권장합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#Register
         */

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20211122");

        // 과금방향, [정과금, 역과금] 중 선택기재, 역과금의 경우 역발행세금계산서 발행시에만 가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /*********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(testCorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("20211122-Register002");

        // 공급자 대표자성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 업종");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /*********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호, '-' 제외 10자리
        taxinvoice.setInvoiceeCorpNum("8888888888");

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

        // 공급받는자 담당자명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("wjkim@linkhubcorp.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행시 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /*********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // [필수] 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // [필수] 세액 합계
        taxinvoice.setTaxTotal("10000");

        // [필수] 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 기재 상 일련번호
        taxinvoice.setSerialNum("123");

        // 기재 상 현금
        taxinvoice.setCash("");

        // 기재 상 수표
        taxinvoice.setChkBill("");

        // 기재 상 어음
        taxinvoice.setNote("");

        // 기재 상 외상미수금
        taxinvoice.setCredit("");

        // 기재 상 비고
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");
        taxinvoice.setKwon((short) 1);
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부
        taxinvoice.setBankBookYN(false);

        /*********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) 
         * - 수정세금계산서 관련 정보는 연동매뉴얼 또는 개발가이드 링크 참조 & 
         * - [참고] 수정세금계산서 작성방법 안내 [https://docs.popbill.com/taxinvoice/modify?lang=java]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 원본세금계산서 국세청승인번호
        taxinvoice.setOrgNTSConfirmNum("");

        /*********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211122"); // 거래일자
        detail.setItemName("품목명");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고");

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211122"); // 거래일자
        detail.setItemName("품목명2");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2");

        taxinvoice.getDetailList().add(detail);

        /*********************************************************************
         * 추가담당자 정보
         *********************************************************************/

        taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        addContact.setSerialNum(1);
        addContact.setContactName("추가 담당자명");
        addContact.setEmail("test2@test.com");

        taxinvoice.getAddContactList().add(addContact);

        try {

            Response response = taxinvoiceService.register(testCorpNum, taxinvoice);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "update", method = RequestMethod.GET)
    public String update(Model m) {
        /*
         * "임시저장" 상태의 세금계산서를 수정합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#Update
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-Register001";

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20211111");

        // 과금방향, [정과금, 역과금] 중 선택기재, 역과금의 경우 역발행세금계산서 발행시에만 가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("정발행");

        // [영수, 청구] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /*********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum(testCorpNum);

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey(mgtKey);

        // 공급자 대표자성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명_수정");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소_수정");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 업종");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /*********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호, '-' 제외 10자리
        taxinvoice.setInvoiceeCorpNum("8888888888");

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

        // 공급받는자 담당자명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행시 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /*********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // [필수] 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // [필수] 세액 합계
        taxinvoice.setTaxTotal("10000");

        // [필수] 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 기재 상 일련번호
        taxinvoice.setSerialNum("123");

        // 기재 상 현금
        taxinvoice.setCash("");

        // 기재 상 수표
        taxinvoice.setChkBill("");

        // 기재 상 어음
        taxinvoice.setNote("");

        // 기재 상 외상미수금
        taxinvoice.setCredit("");

        // 기재 상 비고
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");
        taxinvoice.setKwon((short) 1);
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부
        taxinvoice.setBankBookYN(false);

        /*********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) 
         * - 수정세금계산서 관련 정보는 연동매뉴얼 또는 개발가이드 링크 참조 
         * - [참고] 수정세금계산서 작성방법 안내 [https://docs.popbill.com/taxinvoice/modify?lang=java]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 원본세금계산서 국세청승인번호
        taxinvoice.setOrgNTSConfirmNum("");

        /*********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/

        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211111"); // 거래일자
        detail.setItemName("품목명"); // 품목
        detail.setSpec("규격"); // 규격
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고");

        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();

        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211111"); // 거래일자
        detail.setItemName("품목명2"); // 품목명
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2");

        taxinvoice.getDetailList().add(detail);

        /*********************************************************************
         * 추가담당자 정보
         *********************************************************************/

        taxinvoice.setAddContactList(new ArrayList<TaxinvoiceAddContact>());

        TaxinvoiceAddContact addContact = new TaxinvoiceAddContact();

        addContact.setSerialNum(1);
        addContact.setContactName("추가 담당자명");
        addContact.setEmail("test2@test.com");

        taxinvoice.getAddContactList().add(addContact);

        try {

            Response response = taxinvoiceService.update(testCorpNum, mgtKeyType, mgtKey, taxinvoice);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "issue", method = RequestMethod.GET)
    public String issue(Model m) {
        /*
         * "임시저장" 또는 "(역)발행대기" 상태의 세금계산서를 발행(전자서명)하며, "발행완료" 상태로 처리합니다. 
         * - 세금계산서 국세청 전송정책 : https://docs.popbill.com/taxinvoice/ntsSendPolicy?lang=java 
         * - https://docs.popbill.com/taxinvoice/java/api#TIIssue
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-Register002";

        // 메모
        String memo = "발행 메모";

        // 지연발행 강제여부
        Boolean forceIssue = false;

        try {

            IssueResponse response = taxinvoiceService.issue(testCorpNum, mgtKeyType, mgtKey, memo, forceIssue,
                    testUserID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/issueResponse";
    }

    @RequestMapping(value = "cancelIssue", method = RequestMethod.GET)
    public String cancelIssue(Model m) {
        /*
         * 국세청 전송 이전 "발행완료" 상태의 세금계산서를 "발행취소"하고 국세청 전송 대상에서 제외합니다. 
         * - Delete(삭제)함수를 호출하여 "발행취소" 상태의 전자세금계산서를 삭제하면, 문서번호 재사용이 가능합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#CancelIssue
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-Register002";

        // 메모
        String memo = "발행취소 메모";

        try {

            Response response = taxinvoiceService.cancelIssue(testCorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }
    
    @RequestMapping(value = "delete", method = RequestMethod.GET)
    public String delete(Model m) {
        /*
         * 삭제 가능한 상태의 세금계산서를 삭제합니다. 
         * - 삭제 가능한 상태: "임시저장", "발행취소", "역발행거부", "역발행취소", "전송실패" 
         * - 세금계산서를 삭제해야만 문서번호(mgtKey)를 재사용할 수 있습니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#Delete
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-Register002";

        try {

            Response response = taxinvoiceService.delete(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "registRequest", method = RequestMethod.GET)
    public String registRequest(Model m) {
        /*
         * 공급받는자가 작성한 세금계산서 데이터를 팝빌에 저장하고 공급자에게 송부하여 발행을 요청합니다. 
         * - 역발행 세금계산서 프로세스를 구현하기위해서는 공급자/공급받는자가 모두 팝빌에 회원이여야 합니다. 
         * - 역발행 즉시요청후 공급자가 [발행] 처리시 포인트가 차감되며 역발행 세금계산서 항목중 과금방향(ChargeDirection)에 기재한 값에 따라 정과금(공급자과금) 또는 역과금(공급받는자과금) 처리됩니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#RegistRequest
         */

        // 세금계산서 정보 객체
        Taxinvoice taxinvoice = new Taxinvoice();

        // 작성일자, 날짜형식(yyyyMMdd)
        taxinvoice.setWriteDate("20211122");

        // 과금방향, [정과금, 역과금] 중 선택기재, "역과금"은 역발행세금계산서 발행에만 가능
        taxinvoice.setChargeDirection("정과금");

        // 발행유형, [정발행, 역발행, 위수탁] 중 기재
        taxinvoice.setIssueType("역발행");

        // [영수, 청구] 중 기재
        taxinvoice.setPurposeType("영수");

        // 과세형태, [과세, 영세, 면세] 중 기재
        taxinvoice.setTaxType("과세");

        /*********************************************************************
         * 공급자 정보
         *********************************************************************/

        // 공급자 사업자번호
        taxinvoice.setInvoicerCorpNum("8888888888");

        // 공급자 종사업장 식별번호, 필요시 기재. 형식은 숫자 4자리.
        taxinvoice.setInvoicerTaxRegID("");

        // 공급자 상호
        taxinvoice.setInvoicerCorpName("공급자 상호");

        // 공급자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoicerMgtKey("");

        // 공급자 대표자성명
        taxinvoice.setInvoicerCEOName("공급자 대표자 성명");

        // 공급자 주소
        taxinvoice.setInvoicerAddr("공급자 주소");

        // 공급자 종목
        taxinvoice.setInvoicerBizClass("공급자 업종");

        // 공급자 업태
        taxinvoice.setInvoicerBizType("공급자 업태,업태2");

        // 공급자 담당자 성명
        taxinvoice.setInvoicerContactName("공급자 담당자명");

        // 공급자 담당자 메일주소
        taxinvoice.setInvoicerEmail("test@test.com");

        // 공급자 담당자 연락처
        taxinvoice.setInvoicerTEL("070-7070-0707");

        // 공급자 담당자 휴대폰번호
        taxinvoice.setInvoicerHP("010-000-2222");

        // 발행 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoicerSMSSendYN(false);

        /*********************************************************************
         * 공급받는자 정보
         *********************************************************************/

        // 공급받는자 구분, [사업자, 개인, 외국인] 중 기재
        taxinvoice.setInvoiceeType("사업자");

        // 공급받는자 사업자번호, '-' 제외 10자리
        taxinvoice.setInvoiceeCorpNum(testCorpNum);

        // 공급받는자 상호
        taxinvoice.setInvoiceeCorpName("공급받는자 상호");

        // [역발행시 필수] 공급받는자 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        taxinvoice.setInvoiceeMgtKey("20210701-Request");

        // 공급받는자 대표자 성명
        taxinvoice.setInvoiceeCEOName("공급받는자 대표자 성명");

        // 공급받는자 주소
        taxinvoice.setInvoiceeAddr("공급받는자 주소");

        // 공급받는자 종목
        taxinvoice.setInvoiceeBizClass("공급받는자 업종");

        // 공급받는자 업태
        taxinvoice.setInvoiceeBizType("공급받는자 업태");

        // 공급받는자 담당자명
        taxinvoice.setInvoiceeContactName1("공급받는자 담당자명");

        // 공급받는자 담당자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        taxinvoice.setInvoiceeEmail1("test@invoicee.com");

        // 공급받는자 담당자 연락처
        taxinvoice.setInvoiceeTEL1("070-111-222");

        // 공급받는자 담당자 휴대폰번호
        taxinvoice.setInvoiceeHP1("010-111-222");

        // 역발행시 안내문자메시지 전송여부
        // - 전송시 포인트 차감되며, 전송실패시 환불처리
        taxinvoice.setInvoiceeSMSSendYN(false);

        /*********************************************************************
         * 세금계산서 기재정보
         *********************************************************************/

        // [필수] 공급가액 합계
        taxinvoice.setSupplyCostTotal("100000");

        // [필수] 세액 합계
        taxinvoice.setTaxTotal("10000");

        // [필수] 합계금액, 공급가액 + 세액
        taxinvoice.setTotalAmount("110000");

        // 기재 상 일련번호
        taxinvoice.setSerialNum("");

        // 기재 상 현금
        taxinvoice.setCash("");

        // 기재 상 수표
        taxinvoice.setChkBill("");

        // 기재 상 어음
        taxinvoice.setNote("");

        // 기재 상 외상미수금
        taxinvoice.setCredit("");

        // 기재 상 비고
        taxinvoice.setRemark1("비고1");
        taxinvoice.setRemark2("비고2");
        taxinvoice.setRemark3("비고3");
        taxinvoice.setKwon((short) 1);
        taxinvoice.setHo((short) 1);

        // 사업자등록증 이미지 첨부여부
        taxinvoice.setBusinessLicenseYN(false);

        // 통장사본 이미지 첨부여부
        taxinvoice.setBankBookYN(false);

        /*********************************************************************
         * 수정세금계산서 정보 (수정세금계산서 작성시 기재) 
         * - 수정세금계산서 관련 정보는 연동매뉴얼 또는 개발가이드 링크 참조 & 
         * - [참고] 수정세금계산서 작성방법 안내 [https://docs.popbill.com/taxinvoice/modify?lang=java]
         *********************************************************************/
        // 수정사유코드, 수정사유에 따라 1~6 중 선택기재.
        taxinvoice.setModifyCode(null);

        // 원본세금계산서 국세청승인번호
        taxinvoice.setOrgNTSConfirmNum("");

        /*********************************************************************
         * 상세항목(품목) 정보
         *********************************************************************/
        taxinvoice.setDetailList(new ArrayList<TaxinvoiceDetail>());

        // 상세항목 객체
        TaxinvoiceDetail detail = new TaxinvoiceDetail();
        detail.setSerialNum((short) 1); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211122"); // 거래일자
        detail.setItemName("품목명");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고");
        taxinvoice.getDetailList().add(detail);

        detail = new TaxinvoiceDetail();
        detail.setSerialNum((short) 2); // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20211122"); // 거래일자
        detail.setItemName("품목명2");
        detail.setSpec("규격");
        detail.setQty("1"); // 수량
        detail.setUnitCost("50000"); // 단가
        detail.setSupplyCost("50000"); // 공급가액
        detail.setTax("5000"); // 세액
        detail.setRemark("품목비고2");
        taxinvoice.getDetailList().add(detail);

        // 메모
        String Memo = "즉시요청 메모";

        try {

            Response response = taxinvoiceService.registRequest(testCorpNum, taxinvoice, Memo, testUserID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "request", method = RequestMethod.GET)
    public String request(Model m) {
        /*
         * 공급받는자가 저장된 역발행 세금계산서를 공급자에게 송부하여 발행 요청합니다. 
         * - 역발행 세금계산서 프로세스를 구현하기 위해서는 공급자/공급받는자가 모두 팝빌에 회원이여야 합니다. 
         * - 역발행 요청후 공급자가 [발행] 처리시 포인트가 차감되며 역발행 세금계산서 항목중 과금방향(ChargeDirection)에 기재한 값에 따라 정과금(공급자과금) 또는 역과금(공급받는자과금) 처리됩니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#Request
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.BUY;

        // 세금계산서 문서번호
        String mgtKey = "20211122-Request001";

        // 메모
        String memo = "역발행 요청 메모";

        try {

            Response response = taxinvoiceService.request(testCorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "cancelRequest", method = RequestMethod.GET)
    public String cancelRequest(Model m) {
        /*
         * 공급자가 요청받은 역발행 세금계산서를 발행하기 전, 공급받는자가 역발행요청을 취소합니다. 
         * - [취소]한 세금계산서의 문서번호를 재사용하기 위해서는 삭제 (Delete API)를 호출해야 합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#CancelRequest
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.BUY;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001";

        // 메모
        String memo = "역발행 취소 메모";

        try {

            Response response = taxinvoiceService.cancelRequest(testCorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "refuse", method = RequestMethod.GET)
    public String refuse(Model m) {
        /*
         * 공급자가 공급받는자에게 역발행 요청 받은 세금계산서의 발행을 거부합니다. 
         * - 세금계산서의 문서번호를 재사용하기 위해서는 삭제 (Delete API)를 호출하여 [삭제] 처리해야 합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#Refuse
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20210702-001";

        // 메모
        String memo = "역발행 거부 메모";

        try {

            Response response = taxinvoiceService.refuse(testCorpNum, mgtKeyType, mgtKey, memo);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "sendToNTS", method = RequestMethod.GET)
    public String sendToNTS(Model m) {
        /*
         * 공급자가 "발행완료" 상태의 전자세금계산서를 국세청에 즉시 전송하며, 함수 호출 후 최대 30분 이내에 전송 처리가 완료됩니다. 
         * - 국세청 즉시전송을 호출하지 않은 세금계산서는 발행일 기준 익일 오후 3시에 팝빌 시스템에서 일괄적으로 국세청으로 전송합니다. 
         * - 익일전송시 전송일이 법정공휴일인 경우 다음 영업일에 전송됩니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#SendToNTS
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-95";

        try {

            Response response = taxinvoiceService.sendToNTS(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getInfo", method = RequestMethod.GET)
    public String getInfo(Model m) {
        /*
         * 세금계산서 1건의 상태 및 요약정보를 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetInfo
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-95";

        try {

            TaxinvoiceInfo taxinvoiceInfo = taxinvoiceService.getInfo(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("TaxinvoiceInfo", taxinvoiceInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceInfo";
    }

    @RequestMapping(value = "getInfos", method = RequestMethod.GET)
    public String getInfos(Model m) {
        /*
         * 다수건의 세금계산서 상태 및 요약 정보를 확인합니다. (1회 호출 시 최대 1,000건 확인 가능) 
         * - https://docs.popbill.com/taxinvoice/java/api#GetInfos
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호 배열, 최대 1000건
        String[] MgtKeyList = new String[] { "20211122-001-91", "20211122-001-92", "20211122-001-93" };

        try {

            TaxinvoiceInfo[] taxinvoiceInfos = taxinvoiceService.getInfos(testCorpNum, mgtKeyType, MgtKeyList);

            m.addAttribute("TaxinvoiceInfos", taxinvoiceInfos);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceInfo";
    }

    @RequestMapping(value = "getDetailInfo", method = RequestMethod.GET)
    public String getDetailInfo(Model m) {
        /*
         * 세금계산서 1건의 상세정보를 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetDetailInfo
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            Taxinvoice taxinvoice = taxinvoiceService.getDetailInfo(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Taxinvoice", taxinvoice);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/Taxinvoice";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /*
         * 검색조건에 해당하는 세금계산서를 조회합니다. (조회기간 단위 : 최대 6개월) 
         * - https://docs.popbill.com/taxinvoice/java/api#Search
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 일자유형, R-등록일자, W-작성일자, I-발행일자
        String DType = "W";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20210701";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20210710";

        // 세금계산서 상태코드 배열, 2,3번째 자리에 와일드카드(*) 사용 가능
        String[] State = { "3**", "6**" };

        // 문서유형 배열 N-일반세금계산서, M-수정세금계산서
        String[] Type = { "N", "M" };

        // 과세형태 배열, T-과세, N-면세, Z-영세
        String[] TaxType = { "T", "N", "Z" };

        // 발행형태 배열, N-정발행, R-역발행, T-위수탁
        String[] IssueType = { "N", "R", "T" };

        // 등록유형 배열, P-팝빌, H-홈택스, 외부ASP
        String[] RegType = { "P", "H" };

        // 공급받는자 휴폐업상태 배열, N-미확인, 0-미등록, 1-사업, 2-폐업, 3-휴업
        String[] CloseDownState = { "N", "0", "1", "2", "3" };

        // 지연발행 여부, null 전체조회, true - 지연발행, false- 정상발행
        Boolean LateOnly = null;

        // 종사업장번호 유형, S-공급자, B-공급받는자, T-수탁자
        String TaxRegIDType = "";

        // 종사업장번호 배열
        String TaxRegID = "";

        // 종사업장번호 조회 유무
        String TaxRegIDYN = "";

        // 통합검색어, 공급받는자 거래처명 또는 사업자등록 번호로 조회, 공백시 전체조회
        String QString = "";

        // 문서번호 또는 국세청승인번호 조회
        String MgtKey = "";

        // 페이지 번호
        int Page = 1;

        // 페이지당 목록개수
        int PerPage = 20;

        // 정렬방향, A-오름차순, D-내림차순
        String Order = "D";

        // 연동문서 여부, 공백-전체조회, 0-일반문서, 1-연동문서
        // 일반문서 - 세금계산서 작성시 API가 아닌 팝빌 사이트를 통해 등록한 문서
        String InterOPYN = "";

        try {

            TISearchResult searchResult = taxinvoiceService.Search(testCorpNum, mgtKeyType, DType, SDate, EDate, State,
                    Type, TaxType, IssueType, LateOnly, TaxRegIDType, TaxRegID, TaxRegIDYN, QString, Page, PerPage,
                    Order, InterOPYN, RegType, CloseDownState, MgtKey);

            m.addAttribute("SearchResult", searchResult);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/SearchResult";
    }

    @RequestMapping(value = "getLogs", method = RequestMethod.GET)
    public String getLogs(Model m) {
        /*
         * 세금계산서의 상태에 대한 변경이력을 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetLogs
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            TaxinvoiceLog[] taxinvoiceLogs = taxinvoiceService.getLogs(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("TaxinvoiceLogs", taxinvoiceLogs);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/TaxinvoiceLog";
    }

    @RequestMapping(value = "getURL", method = RequestMethod.GET)
    public String getURL(Model m) {
        /*
         * 로그인 상태로 팝빌 사이트의 전자세금계산서 문서함 메뉴에 접근할 수 있는 페이지의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetURL
         */

        // TBOX : 임시문서함 , SBOX : 매출문서함 , PBOX : 매입문서함 , WRITE : 매출작성
        String TOGO = "SBOX";

        try {

            String url = taxinvoiceService.getURL(testCorpNum, TOGO);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPopUpURL", method = RequestMethod.GET)
    public String getPopUpURL(Model m) {
        /*
         * 팝빌 사이트와 동일한 세금계산서 1건의 상세 정보 페이지의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetPopUpURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getPopUpURL(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getViewURL", method = RequestMethod.GET)
    public String getViewURL(Model m) {
        /*
         * 팝빌 사이트와 동일한 세금계산서 1건의 상세정보 페이지(사이트 상단, 좌측 메뉴 및 버튼 제외)의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetViewURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getViewURL(testCorpNum, mgtKeyType, mgtKey);

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
         * 세금계산서 1건을 인쇄하기 위한 페이지의 팝업 URL을 반환하며, 페이지내에서 인쇄 설정값을 "공급자" / "공급받는자" / "공급자+공급받는자"용 중 하나로 지정할 수 있습니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetPrintURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getPrintURL(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getOldPrintURL", method = RequestMethod.GET)
    public String getOldPrintURL(Model m) {
        /*
         * 세금계산서 1건을 구버전 양식으로 인쇄하기 위한 페이지의 팝업 URL을 반환하며, 페이지내에서 인쇄 설정값을 "공급자" / "공급받는자" / "공급자+공급받는자"용 중 하나로 지정할 수 있습니다.. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetOldPrintURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getOldPrintURL(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";

    }

    @RequestMapping(value = "getEPrintURL", method = RequestMethod.GET)
    public String getEPrintURL(Model m) {
        /*
         * "공급받는자" 용 세금계산서 1건을 인쇄하기 위한 페이지의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetEPrintURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getEPrintURL(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getMassPrintURL", method = RequestMethod.GET)
    public String getMassPrintURL(Model m) {
        /*
         * 다수건의 세금계산서를 인쇄하기 위한 페이지의 팝업 URL을 반환합니다. (최대 100건) 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetMassPrintURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 문서번호 배열, 최대 100건
        String[] MgtKeyList = new String[] { "20211122-001-91", "20211122-001-92" };

        try {

            String url = taxinvoiceService.getMassPrintURL(testCorpNum, mgtKeyType, MgtKeyList);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getMailURL", method = RequestMethod.GET)
    public String getMailURL(Model m) {
        /*
         * 안내메일과 관련된 전자세금계산서를 확인 할 수 있는 상세 페이지의 팝업 URL을 반환하며, 해당 URL은 메일 하단의 "전자세금계산서 보기" 버튼의 링크와 같습니다. 
         * - 함수 호출로 반환 받은 URL에는 유효시간이 없습니다. - https://docs.popbill.com/taxinvoice/java/api#GetMailURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getMailURL(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPDFURL", method = RequestMethod.GET)
    public String getPDFURL(Model m) {
        /*
         * 전자세금계산서 PDF 파일을 다운 받을 수 있는 URL을 반환합니다. 
         * - 반환되는 URL은 보안정책상 30초의 유효시간을 갖으며, 유효시간 이후 호출시 정상적으로 페이지가 호출되지 않습니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetPDFURL
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        try {

            String url = taxinvoiceService.getPDFURL(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getSealURL", method = RequestMethod.GET)
    public String getSealURL(Model m) {
        /*
         * 세금계산서에 첨부할 인감, 사업자등록증, 통장사본을 등록하는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/taxinvoice/java/api#GetSealURL
         */

        try {

            String url = taxinvoiceService.getSealURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "attachFile", method = RequestMethod.GET)
    public String attachFile(Model m) {
        /*
         * "임시저장" 상태의 세금계산서에 1개의 파일을 첨부합니다. (최대 5개) 
         * - https://docs.popbill.com/taxinvoice/java/api#AttachFile
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        // 첨부파일 표시명
        String displayName = "첨부파일.jpg";

        // 첨부할 파일의 InputStream. 예제는 resource에 테스트파일을 참조함.
        // FileInputStream으로 처리하는 것을 권함.
        InputStream stream = getClass().getClassLoader().getResourceAsStream("static/image/test.jpg");
        
        try {

            Response response = taxinvoiceService.attachFile(testCorpNum, mgtKeyType, mgtKey, displayName, stream);

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
        /*
         * "임시저장" 상태의 세금계산서에 첨부된 1개의 파일을 삭제합니다. 
         * - 파일을 식별하는 파일아이디는 첨부파일 목록(GetFiles API) 의 응답항목 중 파일아이디(AttachedFile) 값을 통해 확인할 수 있습니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#DeleteFile
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20210701-01";

        // 파일아이디, getFiles()로 확인한 AttachedFile의 attachedFile 참조.
        String FileID = " 418DD6F7-5358-46A8-B430-04F79CC3D9DA.PBF";

        try {
            Response response = taxinvoiceService.deleteFile(testCorpNum, mgtKeyType, mgtKey, FileID);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getFiles", method = RequestMethod.GET)
    public String getFiles(Model m) {
        /*
         * 세금계산서에 첨부된 파일목록을 확인합니다. 
         * - 응답항목 중 파일아이디(AttachedFile) 항목은 파일삭제(DeleteFile API) 호출시 이용할 수 있습니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetFiles
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20210701-001";

        try {

            AttachedFile[] attachedFiles = taxinvoiceService.getFiles(testCorpNum, mgtKeyType, mgtKey);

            m.addAttribute("AttachedFiles", attachedFiles);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/AttachedFile";
    }

    @RequestMapping(value = "sendEmail", method = RequestMethod.GET)
    public String sendEmail(Model m) {
        /*
         * 세금계산서와 관련된 안내 메일을 재전송 합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#SendEmail
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20210701-001";

        // 수신메일주소
        String receiverMail = "test@test.com";

        try {

            Response response = taxinvoiceService.sendEmail(testCorpNum, mgtKeyType, mgtKey, receiverMail);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "sendSMS", method = RequestMethod.GET)
    public String sendSMS(Model m) {
        /*
         * 세금계산서와 관련된 안내 SMS(단문) 문자를 재전송하는 함수로, 팝빌 사이트 [문자·팩스] > [문자] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다. 
         * - 메시지는 최대 90byte까지 입력 가능하고, 초과한 내용은 자동으로 삭제되어 전송합니다. (한글 최대 45자) - 함수 호출시 포인트가 과금됩니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#SendSMS
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20210701-001";

        // 발신번호
        String senderNum = "07043042991";

        // 수신번호
        String receiverNum = "010111222";

        // 메시지 내용, 90byte 초과시 길이가 조정되어 전송됨
        String contents = "문자 메시지 내용입니다. 세금계산서가 발행되었습니다.";

        try {

            Response response = taxinvoiceService.sendSMS(testCorpNum, mgtKeyType, mgtKey, senderNum, receiverNum,
                    contents);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "sendFAX", method = RequestMethod.GET)
    public String sendFAX(Model m) {
        /*
         * 세금계산서를 팩스로 전송하는 함수로, 팝빌 사이트 [문자·팩스] > [팩스] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다. 
         * - 함수 호출시 포인트가 과금됩니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#SendFAX
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20210701-001";

        // 발신번호
        String senderNum = "07043042991";

        // 수신팩스번호
        String receiverNum = "070111222";

        try {

            Response response = taxinvoiceService.sendFAX(testCorpNum, mgtKeyType, mgtKey, senderNum, receiverNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "attachStatement", method = RequestMethod.GET)
    public String attachStatement(Model m) {
        /*
         * 팝빌 전자명세서 API를 통해 발행한 전자명세서를 세금계산서에 첨부합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#AttachStatement
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        // 첨부할 명세서 코드 - 121(거래명세서), 122(청구서), 123(견적서), 124(발주서), 125(입금표), 126(영수증)
        int subItemCode = 121;

        // 첨부활 전자명세서 문서번호
        String subMgtKey = "20211122-001-92-STATEMENT";

        try {

            Response response = taxinvoiceService.attachStatement(testCorpNum, mgtKeyType, mgtKey, subItemCode,
                    subMgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "detachStatement", method = RequestMethod.GET)
    public String detachStatement(Model m) {
        /*
         * 세금계산서에 첨부된 전자명세서를 해제합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#DetachStatement
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 문서번호
        String mgtKey = "20211122-001-92";

        // 첨부해제할 명세서 코드 - 121(거래명세서), 122(청구서), 123(견적서), 124(발주서), 125(입금표), 126(영수증)
        int subItemCode = 121;

        // 첨부해제할 전자명세서 문서번호
        String subMgtKey = "20211122-001-92-STATEMENT";

        try {

            Response response = taxinvoiceService.detachStatement(testCorpNum, mgtKeyType, mgtKey, subItemCode,
                    subMgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getEmailPublicKeys", method = RequestMethod.GET)
    public String getEmailPublicKeys(Model m) {
        /*
         * 전자세금계산서 유통사업자의 메일 목록을 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetEmailPublicKeys
         */

        try {

            EmailPublicKey[] emailPublicKeys = taxinvoiceService.getEmailPublicKeys(testCorpNum);

            m.addAttribute("EmailPublicKeys", emailPublicKeys);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Taxinvoice/EmailPublicKey";
    }

    @RequestMapping(value = "assignMgtKey", method = RequestMethod.GET)
    public String assignMgtKey(Model m) {
        /*
         * 팝빌 사이트를 통해 발행하였지만 문서번호가 존재하지 않는 세금계산서에 문서번호를 할당합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#AssignMgtKey
         */

        // 세금계산서 유형, 매출-SELL, 매입-BUY, 위수탁-TRUSTEE
        MgtKeyType mgtKeyType = MgtKeyType.SELL;

        // 세금계산서 아이템키, 문서 목록조회(Search) API의 반환항목중 ItemKey 참조
        String itemKey = "021112211014200001";

        // 할당할 문서번호, 최대 24자리, 영문, 숫자 '-', '_'를 조합하여 사업자별로 중복되지 않도록 구성
        String mgtKey = "20210701-20202";

        try {

            Response response = taxinvoiceService.assignMgtKey(testCorpNum, mgtKeyType, itemKey, mgtKey);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "listEmailConfig", method = RequestMethod.GET)
    public String listEmailConfig(Model m) {
        /*
         * 세금계산서 관련 메일 항목에 대한 발송설정을 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#ListEmailConfig
         */
        Map<String, Boolean> emailSendConfigs = new HashMap<String, Boolean>();
        
        try {

            EmailSendConfig[] Configs = taxinvoiceService.listEmailConfig(testCorpNum);
            
            for(EmailSendConfig emailSendConfig : Configs) {
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
        /*
         * 세금계산서 관련 메일 항목에 대한 발송설정을 수정합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#UpdateEmailConfig
         *
         * 메일전송유형 [정발행] TAX_ISSUE : 공급받는자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다.
         * TAX_ISSUE_INVOICER : 공급자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다. TAX_CHECK : 공급자에게
         * 전자세금계산서가 수신확인 되었음을 알려주는 메일입니다. TAX_CANCEL_ISSUE : 공급받는자에게 전자세금계산서가 발행취소 되었음을
         * 알려주는 메일입니다.
         *
         * [발행예정] TAX_SEND : 공급받는자에게 [발행예정] 세금계산서가 발송 되었음을 알려주는 메일입니다. TAX_ACCEPT :
         * 공급자에게 [발행예정] 세금계산서가 승인 되었음을 알려주는 메일입니다. TAX_ACCEPT_ISSUE : 공급자에게 [발행예정]
         * 세금계산서가 자동발행 되었음을 알려주는 메일입니다. TAX_DENY : 공급자에게 [발행예정] 세금계산서가 거부 되었음을 알려주는
         * 메일입니다. TAX_CANCEL_SEND : 공급받는자에게 [발행예정] 세금계산서가 취소 되었음을 알려주는 메일입니다.
         *
         * [역발행] TAX_REQUEST : 공급자에게 세금계산서를 전자서명 하여 발행을 요청하는 메일입니다. TAX_CANCEL_REQUEST :
         * 공급받는자에게 세금계산서가 취소 되었음을 알려주는 메일입니다. TAX_REFUSE : 공급받는자에게 세금계산서가 거부 되었음을 알려주는
         * 메일입니다.
         *
         * [위수탁발행] TAX_TRUST_ISSUE : 공급받는자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다.
         * TAX_TRUST_ISSUE_TRUSTEE : 수탁자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다.
         * TAX_TRUST_ISSUE_INVOICER : 공급자에게 전자세금계산서가 발행 되었음을 알려주는 메일입니다.
         * TAX_TRUST_CANCEL_ISSUE : 공급받는자에게 전자세금계산서가 발행취소 되었음을 알려주는 메일입니다.
         * TAX_TRUST_CANCEL_ISSUE_INVOICER : 공급자에게 전자세금계산서가 발행취소 되었음을 알려주는 메일입니다.
         *
         * [위수탁 발행예정] TAX_TRUST_SEND : 공급받는자에게 [발행예정] 세금계산서가 발송 되었음을 알려주는 메일입니다.
         * TAX_TRUST_ACCEPT : 수탁자에게 [발행예정] 세금계산서가 승인 되었음을 알려주는 메일입니다.
         * TAX_TRUST_ACCEPT_ISSUE : 수탁자에게 [발행예정] 세금계산서가 자동발행 되었음을 알려주는 메일입니다.
         * TAX_TRUST_DENY : 수탁자에게 [발행예정] 세금계산서가 거부 되었음을 알려주는 메일입니다.
         * TAX_TRUST_CANCEL_SEND : 공급받는자에게 [발행예정] 세금계산서가 취소 되었음을 알려주는 메일입니다.
         *
         * [처리결과] TAX_CLOSEDOWN : 거래처의 휴폐업 여부를 확인하여 안내하는 메일입니다. TAX_NTSFAIL_INVOICER :
         * 전자세금계산서 국세청 전송실패를 안내하는 메일입니다.
         *
         * [정기발송] TAX_SEND_INFO : 전월 귀속분 [매출 발행 대기] 세금계산서의 발행을 안내하는 메일입니다.
         * ETC_CERT_EXPIRATION : 팝빌에서 이용중인 공인인증서의 갱신을 안내하는 메일입니다.
         */

        // 메일 전송 유형
        String emailType = "TAX_ISSUE";

        // 전송 여부 (true = 전송, false = 미전송)
        Boolean sendYN = true;

        try {

            Response response = taxinvoiceService.updateEmailConfig(testCorpNum, emailType, sendYN);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getSendToNTSConfig", method = RequestMethod.GET)
    public String getSendToNTSConfig(Model m) {
        /*
         * 연동회원의 국세청 전송 옵션 설정 상태를 확인합니다. 
         * - 국세청 전송 옵션 설정은 팝빌 사이트 [전자세금계산서] > [환경설정] > [세금계산서 관리] 메뉴에서 설정할 수 있으며, API로 설정은 불가능 합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetSendToNTSConfig
         */
        try {

            boolean ntsConfig = taxinvoiceService.getSendToNTSConfig(testCorpNum);

            m.addAttribute("NTSConfig", ntsConfig);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "/Taxinvoice/getSendToNTSConfig";
    }

    @RequestMapping(value = "getTaxCertURL", method = RequestMethod.GET)
    public String getTaxCertURL(Model m) {
        /*
         * 전자세금계산서 발행에 필요한 인증서를 팝빌 인증서버에 등록하기 위한 페이지의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - 인증서 갱신/재발급/비밀번호 변경한 경우, 변경된 인증서를 팝빌 인증서버에 재등록 해야합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetTaxCertURL
         */

        try {

            String url = taxinvoiceService.getTaxCertURL(testCorpNum, testUserID);

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
         * 팝빌 인증서버에 등록된 인증서의 만료일을 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetCertificateExpireDate
         */

        try {

            Date expireDate = taxinvoiceService.getCertificateExpireDate(testCorpNum);

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
         * 팝빌 인증서버에 등록된 인증서의 유효성을 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#CheckCertValidation
         */

        try {

            Response response = taxinvoiceService.checkCertValidation(testCorpNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /*
         * 전자세금계산서 발행단가를 확인합니다. 
         * - https://docs.popbill.com/taxinvoice/java/api#GetUnitCost
         */

        try {

            float unitCost = taxinvoiceService.getUnitCost(testCorpNum);

            m.addAttribute("Result", unitCost);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getChargeInfo", method = RequestMethod.GET)
    public String chargeInfo(Model m) {
        /*
         * 팝빌 전자세금계산서 API 서비스 과금정보를 확인합니다.
         * - https://docs.popbill.com/taxinvoice/java/api#GetChargeInfo
         */

        try {

            ChargeInfo chrgInfo = taxinvoiceService.getChargeInfo(testCorpNum);

            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }
}
