/**
  * 팝빌 전자명세서 API Java SDK SpringBoot Example
  *
  * SpringBoot 연동 튜토리얼 안내 : https://developers.popbill.com/guide/statement/java/getting-started/tutorial?fwn=springboot
  * 연동 기술지원 연락처 : 1600-9854
  * 연동 기술지원 이메일 : code@linkhubcorp.com
  *
  */
package com.popbill.example.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.popbill.api.AttachedFile;
import com.popbill.api.ChargeInfo;
import com.popbill.api.EmailSendConfig;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.SMTIssueResponse;
import com.popbill.api.StatementService;
import com.popbill.api.statement.Statement;
import com.popbill.api.statement.StatementDetail;
import com.popbill.api.statement.StatementInfo;
import com.popbill.api.statement.StatementLog;
import com.popbill.api.statement.StmtSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "StatementService")
public class StatementServiceController {

    @Autowired
    private StatementService statementService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Statement/index";
    }

    @RequestMapping(value = "checkMgtKeyInUse", method = RequestMethod.GET)
    public String checkMgtKeyInUse(Model m) {
        /**
         * 파트너가 전자명세서 관리 목적으로 할당하는 문서번호의 사용여부를 확인합니다.
         * - 이미 사용 중인 문서번호는 중복 사용이 불가하고, 전자명세서가 삭제된 경우에만 문서번호의 재사용이 가능합니다.
         * - https://developers.popbill.com/reference/statement/java/api/info#CheckMgtKeyInUse
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT001";

        String isUseStr;

        try {
            boolean IsUse = statementService.checkMgtKeyInUse(CorpNum, itemCode, mgtKey);
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
         * 작성된 전자명세서 데이터를 팝빌에 저장과 동시에 발행하여, "발행완료" 상태로 처리합니다.
         * - 팝빌 사이트 [전자명세서] > [환경설정] > [전자명세서 관리] 메뉴의 발행시 자동승인 옵션 설정을 통해 전자명세서를 "발행완료" 상태가 아닌 "승인대기" 상태로 발행 처리 할 수 있습니다.
         * - https://developers.popbill.com/reference/statement/java/api/issue#RegistIssue
         */

        // 전자명세서 정보 객체
        Statement statement = new Statement();

        // 명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        statement.setItemCode((short) 121);

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        statement.setMgtKey("20230102-BOOT001");

        // 맞춤양식코드, 미기재시 기본양식으로 처리
        statement.setFormCode("");

        // 작성일자, 형태 yyyyMMdd
        statement.setWriteDate("20230102");

        // {과세, 영세, 면세} 중 기재
        statement.setTaxType("과세");

        // {영수, 청구, 없음} 중 기재
        statement.setPurposeType("영수");

        // 기재상 일련번호 항목
        statement.setSerialNum("123");

        // 세액 합계
        statement.setTaxTotal("40000");

        // 공급가액 합계
        statement.setSupplyCostTotal("400000");

        // 합계금액. 공급가액 + 세액
        statement.setTotalAmount("440000");

        // 기재상 비고 항목
        statement.setRemark1("비고1");
        statement.setRemark2("비고2");
        statement.setRemark3("비고3");

        /*********************************************************************
         *                                발신자 정보
         *********************************************************************/

        // 발신자 사업자번호
        statement.setSenderCorpNum("1234567890");

        // 발신자 종사업장 식별번호, 숫자 4자리, 필요시 기재
        statement.setSenderTaxRegID("");

        // 발신자 상호
        statement.setSenderCorpName("발신자 상호");

        // 발신자 대표자 성명
        statement.setSenderCEOName("발신자 대표자 성명");

        // 발신자 주소
        statement.setSenderAddr("발신자 주소");

        // 발신자 업태
        statement.setSenderBizType("업태");

        // 발신자 종목
        statement.setSenderBizClass("업종");

        // 발신자 담당자 성명
        statement.setSenderContactName("발신자 담당자 성명");

        // 발신자 담당자 연락처
        statement.setSenderTEL("070-7070-0707");

        // 발신자 담당자 휴대폰번호
        statement.setSenderHP("010-000-2222");

        // 발신자 담당자 메일주소
        statement.setSenderEmail("test@test.com");

        /*********************************************************************
         *                            수신자 정보
         *********************************************************************/

        // 수신자 사업자번호
        statement.setReceiverCorpNum("8888888888");

        // 수신자 상호
        statement.setReceiverCorpName("수신자 상호");

        // 수신자 대표자 성명
        statement.setReceiverCEOName("수신자 대표자 성명");

        // 수신자 주소
        statement.setReceiverAddr("수신자 주소");

        // 수신자 업태
        statement.setReceiverBizType("수신자 업태");

        // 수신자 종목
        statement.setReceiverBizClass("수신자 종목");

        // 수신자 담당자 성명
        statement.setReceiverContactName("수신자 담당자 성명");

        // 수신자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        statement.setReceiverEmail("");

        Map<String, String> propertyBag = new HashMap<String, String>();

        propertyBag.put("Balance", "15000");            // 전잔액
        propertyBag.put("Deposit", "5000");             // 입금액
        propertyBag.put("CBalance", "20000");           // 현잔액

        // 추가속성
        // 전자명세서 종류별 추가할 속성을 "key", "value" 형식으로 값을 입력
        statement.setPropertyBag(propertyBag);

        // 사업자등록증 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBusinessLicenseYN(false);

        // 통장사본 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBankBookYN(false);

        // 알림문자 전송 여부
        statement.setSmssendYN(false);

        /*********************************************************************
         *                            전자명세서 품목항목
         *********************************************************************/

        statement.setDetailList(new ArrayList<StatementDetail>());

        StatementDetail detail = new StatementDetail();    // 상세항목(품목) 배열

        detail.setSerialNum((short) 1);                    // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                         // 품목명
        detail.setSpec("규격");                             // 규격
        detail.setQty("1");                                // 수량
        detail.setUnit("200000");                          // 단가
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액
        detail.setRemark("비고1");                          // 비고

        statement.getDetailList().add(detail);

        detail = new StatementDetail();                    // 상세항목(품목) 배열
        detail.setSerialNum((short) 2);                    // 일련번호 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                         // 품목명
        detail.setSpec("규격");                             // 규격
        detail.setQty("1");                                // 수량
        detail.setUnit("200000");                          // 단가
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액
        detail.setRemark("비고1");                          // 비고

        statement.getDetailList().add(detail);

        String Memo = "전자명세서 즉시발행 메모";

        // 발행 안내 메일 제목
        // - 미입력 시 팝빌에서 지정한  이메일 제목으로 전송
        String emailSubject = "";

        try {
            SMTIssueResponse response = statementService.registIssue(CorpNum, statement, Memo, UserID, emailSubject);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/issueResponse";
    }

    @RequestMapping(value = "register", method = RequestMethod.GET)
    public String register(Model m) {
        /**
         * 작성된 전자명세서 데이터를 팝빌에 저장합니다.
         * - "임시저장" 상태의 전자명세서는 발행(Issue API) 함수를 호출하여 "발행완료"처리한 경우에만 수신자에게 발행 안내 메일이 발송됩니다.
         * - https://developers.popbill.com/reference/statement/java/api/issue#Register
         */

        // 전자명세서 정보 객체
        Statement statement = new Statement();

        // 명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        statement.setItemCode((short) 121);

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        statement.setMgtKey("20230102-BOOT001");

        // 맞춤양식코드, 미기재시 기본양식으로 처리
        statement.setFormCode("");

        // 작성일자, 형태 yyyyMMdd
        statement.setWriteDate("20230113");

        // {과세, 영세, 면세} 중 기재
        statement.setTaxType("과세");

        // {영수, 청구, 없음} 중 기재
        statement.setPurposeType("영수");

        // 기재상 일련번호 항목
        statement.setSerialNum("123");

        // 세액 합계
        statement.setTaxTotal("40000");

        // 공급가액 합계
        statement.setSupplyCostTotal("400000");

        // 합계금액. 공급가액 + 세액
        statement.setTotalAmount("440000");

        // 기재상 비고 항목
        statement.setRemark1("비고1");
        statement.setRemark2("비고2");
        statement.setRemark3("비고3");

        /*********************************************************************
         *                                발신자 정보
         *********************************************************************/

        // 발신자 사업자번호
        statement.setSenderCorpNum("1234567890");

        // 발신자 종사업장 식별번호, 숫자 4자리, 필요시 기재
        statement.setSenderTaxRegID("");

        // 발신자 상호
        statement.setSenderCorpName("발신자 상호");

        // 발신자 대표자 성명
        statement.setSenderCEOName("발신자 대표자 성명");

        // 발신자 주소
        statement.setSenderAddr("발신자 주소");

        // 발신자 업태
        statement.setSenderBizType("업태");

        // 발신자 종목
        statement.setSenderBizClass("업종");

        // 발신자 담당자 성명
        statement.setSenderContactName("발신자 담당자 성명");

        // 발신자 담당자 연락처
        statement.setSenderTEL("070-7070-0707");

        // 발신자 담당자 휴대폰번호
        statement.setSenderHP("010-000-2222");

        // 발신자 담당자 메일주소
        statement.setSenderEmail("test@test.com");

        /*********************************************************************
         *                            수신자 정보
         *********************************************************************/

        // 수신자 사업자번호
        statement.setReceiverCorpNum("8888888888");

        // 수신자 상호
        statement.setReceiverCorpName("수신자 상호");

        // 수신자 대표자 성명
        statement.setReceiverCEOName("수신자 대표자 성명");

        // 수신자 주소
        statement.setReceiverAddr("수신자 주소");

        // 수신자 업태
        statement.setReceiverBizType("수신자 업태");

        // 수신자 종목
        statement.setReceiverBizClass("수신자 종목");

        // 수신자 담당자 성명
        statement.setReceiverContactName("수신자 담당자 성명");

        // 수신자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        statement.setReceiverEmail("test@receiver.com");

        // 추가속성
        // 전자명세서 종류별 추가할 속성을 "key", "value" 형식으로 값을 입력
        Map<String, String> propertyBag = new HashMap<String, String>();

        propertyBag.put("Balance", "15000");            // 전잔액
        propertyBag.put("Deposit", "5000");             // 입금액
        propertyBag.put("CBalance", "20000");           // 현잔액

        statement.setPropertyBag(propertyBag);

        // 사업자등록증 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBusinessLicenseYN(false);

        // 통장사본 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBankBookYN(false);

        // 알림문자 전송 여부
        statement.setSmssendYN(false);

        /*********************************************************************
         *                            전자명세서 품목항목
         *********************************************************************/

        statement.setDetailList(new ArrayList<StatementDetail>());

        StatementDetail detail = new StatementDetail();    // 상세항목(품목) 배열

        detail.setSerialNum((short) 1);                    // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                         // 품목명
        detail.setSpec("규격");                             // 규격
        detail.setQty("1");                                // 수량
        detail.setUnit("200000");                          // 단가
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액
        detail.setRemark("비고1");                          // 비고

        statement.getDetailList().add(detail);

        detail = new StatementDetail();                    // 상세항목(품목) 배열
        detail.setSerialNum((short) 2);                    // 일련번호 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                         // 품목명
        detail.setSpec("규격");                             // 규격
        detail.setQty("1");                                // 수량
        detail.setUnit("200000");                          // 단가
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액
        detail.setRemark("비고1");                          // 비고

        statement.getDetailList().add(detail);


        try {
            Response response = statementService.register(CorpNum, statement);
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
         * "임시저장" 상태의 전자명세서를 수정합니다.
         * - https://developers.popbill.com/reference/statement/java/api/issue#Update
         */

        // 명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        String mgtKey = "20230102-BOOT002";

        // 전자명세서 정보 객체
        Statement statement = new Statement();

        // 명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        // statement.setItemCode((short) 121);

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        // statement.setMgtKey(mgtKey);

        // 맞춤양식코드, 미기재시 기본양식으로 처리
        statement.setFormCode("");

        // 작성일자, 형태 yyyyMmdd
        statement.setWriteDate("20230102");

        // {과세, 영세, 면세} 중 기재
        statement.setTaxType("과세");

        // {영수, 청구, 없음} 중 기재
        statement.setPurposeType("영수");

        // 기재상 일련번호 항목
        statement.setSerialNum("123");

        // 세액 합계
        statement.setTaxTotal("40000");

        // 공급가액 합계
        statement.setSupplyCostTotal("400000");

        // 합계금액. 공급가액 + 세액
        statement.setTotalAmount("440000");

        // 기재상 비고 항목
        statement.setRemark1("비고1");
        statement.setRemark2("비고2");
        statement.setRemark3("비고3");

        /*********************************************************************
         *                                발신자 정보
         *********************************************************************/

        // 발신자 사업자번호
        statement.setSenderCorpNum("1234567890");

        // 발신자 종사업장 식별번호, 숫자 4자리, 필요시 기재
        statement.setSenderTaxRegID("");

        // 발신자 상호
        statement.setSenderCorpName("발신자 상호");

        // 발신자 대표자 성명
        statement.setSenderCEOName("발신자 대표자 성명_수정");

        // 발신자 주소
        statement.setSenderAddr("발신자 주소_수정");

        // 발신자 업태
        statement.setSenderBizType("업태");

        // 발신자 종목
        statement.setSenderBizClass("업종");

        // 발신자 담당자 성명
        statement.setSenderContactName("발신자 담당자 성명");

        // 발신자 담당자 연락처
        statement.setSenderTEL("070-7070-0707");

        // 발신자 담당자 휴대폰번호
        statement.setSenderHP("010-000-2222");

        // 발신자 담당자 메일주소
        statement.setSenderEmail("test@test.com");

        /*********************************************************************
         *                            수신자 정보
         *********************************************************************/

        // 수신자 사업자번호
        statement.setReceiverCorpNum("8888888888");

        // 수신자 상호
        statement.setReceiverCorpName("수신자 상호");

        // 수신자 대표자 성명
        statement.setReceiverCEOName("수신자 대표자 성명");

        // 수신자 주소
        statement.setReceiverAddr("수신자 주소");

        // 수신자 업태
        statement.setReceiverBizType("수신자 업태");

        // 수신자 종목
        statement.setReceiverBizClass("수신자 종목");

        // 수신자 담당자 성명
        statement.setReceiverContactName("수신자 담당자 성명");

        // 수신자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        statement.setReceiverEmail("test@receiver.com");

        // 추가속성
        // 전자명세서 종류별 추가할 속성을 "key", "value" 형식으로 값을 입력
        Map<String, String> propertyBag = new HashMap<String, String>();

        propertyBag.put("Balance", "15000");            // 전잔액
        propertyBag.put("Deposit", "5000");             // 입금액
        propertyBag.put("CBalance", "20000");           // 현잔액

        statement.setPropertyBag(propertyBag);

        // 사업자등록증 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBusinessLicenseYN(false);

        // 통장사본 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBankBookYN(false);

        // 알림문자 전송 여부
        statement.setSmssendYN(false);

        /*********************************************************************
         *                            전자명세서 품목항목
         *********************************************************************/

        statement.setDetailList(new ArrayList<StatementDetail>());

        StatementDetail detail = new StatementDetail();    // 상세항목(품목) 배열

        detail.setSerialNum((short) 1);                    // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                         // 품목명
        detail.setSpec("규격");                             // 규격
        detail.setQty("1");                                // 수량
        detail.setUnit("200000");                          // 단가
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액
        detail.setRemark("비고1");                          // 비고

        statement.getDetailList().add(detail);

        detail = new StatementDetail();                    // 상세항목(품목) 배열
        detail.setSerialNum((short) 2);                    // 일련번호 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                         // 품목명
        detail.setSpec("규격");                             // 규격
        detail.setQty("1");                                // 수량
        detail.setUnit("200000");                          // 단가
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액
        detail.setRemark("비고1");                          // 비고

        statement.getDetailList().add(detail);

        try {
            Response response = statementService.update(CorpNum, itemCode, mgtKey, statement);
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
         * "임시저장" 상태의 전자명세서를 발행하여, "발행완료" 상태로 처리합니다.
         * - 팝빌 사이트 [전자명세서] > [환경설정] > [전자명세서 관리] 메뉴의 발행시 자동승인 옵션 설정을 통해
         * - 전자명세서를 "발행완료" 상태가 아닌 "승인대기" 상태로 발행 처리 할 수 있습니다.
         * - 전자명세서 발행 함수 호출시 수신자에게 발행 안내 메일이 발송됩니다.
         * - https://developers.popbill.com/reference/statement/java/api/issue#Issue
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 메모
        String memo = "발행메모";

        // 전자명세서 발행 안내메일 제목
        // - 미입력시 팝빌에서 지정한 이메일 제목으로 전송
        String emailSubject = "테스트";

        try {
            Response response = statementService.issue(CorpNum, itemCode, mgtKey, memo, emailSubject, null);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "cancelIssue", method = RequestMethod.GET)
    public String cancelIssue(Model m) {
        /**
         * 발신자가 발행한 전자명세서를 발행취소합니다.
         * - "발행취소" 상태의 전자명세서를 삭제(Delete API) 함수를 이용하면, 전자명세서 관리를 위해 부여했던 문서번호를 재사용 할 수 있습니다.
         * - https://developers.popbill.com/reference/statement/java/api/issue#Cancel
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 메모
        String memo = "발행취소 메모";

        try {
            Response response = statementService.cancel(CorpNum, itemCode, mgtKey, memo);
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
         * 삭제 가능한 상태의 전자명세서를 삭제합니다.
         * - 삭제 가능한 상태: "임시저장", "취소", "승인거부", "발행취소"
         * - 전자명세서를 삭제하면 사용된 문서번호(mgtKey)를 재사용할 수 있습니다.
         * - https://developers.popbill.com/reference/statement/java/api/issue#Delete
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            Response response = statementService.delete(CorpNum, itemCode, mgtKey);
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
         * 전자명세서의 1건의 상태 및 요약정보 확인합니다.
         * 전자명세서 상태코드 [https://developers.popbill.com/reference/statement/java/response-code#state-code]
         * - https://developers.popbill.com/reference/statement/java/api/info#GetInfo
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            StatementInfo statementInfo = statementService.getInfo(CorpNum, itemCode, mgtKey);
            m.addAttribute("StatementInfo", statementInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/StatementInfo";
    }

    @RequestMapping(value = "getInfos", method = RequestMethod.GET)
    public String getInfos(Model m) {
        /**
         * 다수건의 전자명세서 상태 및 요약 정보를 확인합니다. (1회 호출 시 최대 1,000건 확인 가능)
         * 전자명세서 상태코드 [https://developers.popbill.com/reference/statement/java/response-code#state-code]
         * - https://developers.popbill.com/reference/statement/java/api/info#GetInfos
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호 배열(최대 1000건)
        String[] MgtKeyList = new String[]{"20230102-BOOT001", "20230102-BOOT002"};

        try {
            StatementInfo[] statementInfos = statementService.getInfos(CorpNum, itemCode, MgtKeyList);
            m.addAttribute("StatementInfos", statementInfos);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/StatementInfo";
    }

    @RequestMapping(value = "getDetailInfo", method = RequestMethod.GET)
    public String getDetailInfo(Model m) {
        /**
         * 전자명세서 1건의 상세정보 확인합니다.
         * - https://developers.popbill.com/reference/statement/java/api/info#GetDetailInfo
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            Statement statement = statementService.getDetailInfo(CorpNum, itemCode, mgtKey);
            m.addAttribute("Statement", statement);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/Statement";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /**
         * 검색조건에 해당하는 세금계산서를 조회합니다. (조회기간 단위 : 최대 6개월)
         * 전자명세서 상태코드 [https://developers.popbill.com/reference/statement/java/response-code#state-code]
         * - https://developers.popbill.com/reference/statement/java/api/info#Search
         */

        // 일자 유형 ("R" , "W" , "I" 중 택 1)
        // └ R = 등록일자 , W = 작성일자 , I = 발행일자
        String DType = "W";

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20230102";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20230131";

        // 전자명세서 상태코드 배열 (2,3번째 자리에 와일드카드(*) 사용 가능)
        // - 미입력시 전체조회
        String[] State = {"100", "2**", "3**", "4**"};

        // 전자명세서 문서유형 배열 (121 , 122 , 123 , 124 , 125 , 126 중 선택. 다중 선택 가능)
        // 121 = 명세서 , 122 = 청구서 , 123 = 견적서
        // 124 = 발주서 , 125 = 입금표 , 126 = 영수증
        int[] ItemCode = {121, 122, 123, 124, 125, 126};

        // 통합검색어, 거래처 상호명 또는 거래처 사업자번호로 조회
        // - 미입력시 전체조회
        String QString = "";

        // 페이지 번호
        int Page = 1;

        // 페이지당 목록 건수, 최대 1000건
        int PerPage = 20;

        // 정렬방향, A-오름차순, D-내림차순
        String Order = "D";

        try {
            StmtSearchResult searchResult = statementService.search(CorpNum, DType, SDate, EDate, State, ItemCode,
                    QString, Page, PerPage, Order);
            m.addAttribute("SearchResult", searchResult);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/SearchResult";
    }

    @RequestMapping(value = "getLogs", method = RequestMethod.GET)
    public String getLogs(Model m) {
        /**
         * 전자명세서의 상태에 대한 변경이력을 확인합니다.
         * - https://developers.popbill.com/reference/statement/java/api/info#GetLogs
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            StatementLog[] statementLogs = statementService.getLogs(CorpNum, itemCode, mgtKey);
            m.addAttribute("StatementLogs", statementLogs);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/StatementLog";
    }

    @RequestMapping(value = "getURL", method = RequestMethod.GET)
    public String getURL(Model m) {
        /**
         * 로그인 상태로 팝빌 사이트의 전자명세서 문서함 메뉴에 접근할 수 있는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/statement/java/api/info#GetURL
         */

        // TBOX : 임시문서함 , SBOX : 발행문서함
        String TOGO = "SBOX";

        try {
            String url = statementService.getURL(CorpNum, UserID, TOGO);
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
         * 전자명세서 1건의 상세 정보 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/statement/java/api/view#GetPopUpURL
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            String url = statementService.getPopUpURL(CorpNum, itemCode, mgtKey, UserID);
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
         * 팝빌 사이트와 동일한 전자명세서 1건의 상세 정보 페이지(사이트 상단, 좌측 메뉴 및 버튼 제외)의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/statement/java/api/view#GetViewURL
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            String url = statementService.getViewURL(CorpNum, itemCode, mgtKey, UserID);
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
         * 전자명세서 1건을 인쇄하기 위한 페이지의 팝업 URL을 반환하며, 페이지내에서 인쇄 설정값을 "공급자" / "공급받는자" / "공급자+공급받는자"용 중 하나로 지정할 수 있습니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - 전자명세서의 공급자는 "발신자", 공급받는자는 "수신자"를 나타내는 용어입니다.
         * - https://developers.popbill.com/reference/statement/java/api/view#GetPrintURL
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            String url = statementService.getPrintURL(CorpNum, itemCode, mgtKey, UserID);
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
         * - 전자명세서의 공급받는자는 "수신자"를 나타내는 용어입니다.
         * - https://developers.popbill.com/reference/statement/java/api/view#GetEPrintURL
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            String url = statementService.getEPrintURL(CorpNum, itemCode, mgtKey, UserID);
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
         * 다수건의 전자명세서를 인쇄하기 위한 페이지의 팝업 URL을 반환합니다. (최대 100건)
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/statement/java/api/view#GetMassPrintURL
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 문서번호 배열, 최대 100건
        String[] mgtKeyList = new String[]{"20230102-BOOT001", "20230102-BOOT002"};

        try {
            String url = statementService.getMassPrintURL(CorpNum, itemCode, mgtKeyList, UserID);
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
         * 전자명세서 안내메일의 상세보기 링크 URL을 반환합니다.
         * - 함수 호출로 반환 받은 URL에는 유효시간이 없습니다.
         * - https://developers.popbill.com/reference/statement/java/api/view#GetMailURL
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        try {
            String url = statementService.getMailURL(CorpNum, itemCode, mgtKey, UserID);
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
         * 전자명세서에 첨부할 인감, 사업자등록증, 통장사본을 등록하는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#GetSealURL
         */

        try {
            String url = statementService.getSealURL(CorpNum, UserID);
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
         * "임시저장" 상태의 명세서에 1개의 파일을 첨부합니다. (최대 5개)
         * - https://developers.popbill.com/reference/statement/java/api/etc#AttachFile
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT002";

        // 첨부파일 표시명
        String displayName = "첨부파일.jpg";

        // 첨부할 파일의 InputStream. 예제는 resource에 테스트파일을 참조함.
        // FileInputStream으로 처리하는 것을 권함.
        InputStream stream = getClass().getClassLoader().getResourceAsStream("static/image/test.jpg");

        try {
            Response response = statementService.attachFile(CorpNum, itemCode, mgtKey, displayName, stream);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                }
        }


        return "response";
    }

    @RequestMapping(value = "deleteFile", method = RequestMethod.GET)
    public String deleteFile(Model m) {
        /**
         * "임시저장" 상태의 전자명세서에 첨부된 1개의 파일을 삭제합니다.
         * - 파일을 식별하는 파일아이디는 첨부파일 목록 확인(GetFiles API) 함수의 응답항목 중 파일아이디(AttachedFile) 값을 통해 확인할 수 있습니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#DeleteFile
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT002";

        // getFiles()로 해당 파일의 attachedFile 필드값 기재.
        String FileID = "";

        try {
            Response response = statementService.deleteFile(CorpNum, itemCode, mgtKey, FileID);
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
         * 전자명세서에 첨부된 파일목록을 확인합니다.
         * - 응답항목 중 파일아이디(AttachedFile) 항목은 파일삭제(DeleteFile API) 함수 호출 시 이용할 수 있습니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#GetFiles
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT002";

        try {
            AttachedFile[] attachedFiles = statementService.getFiles(CorpNum, itemCode, mgtKey);
            m.addAttribute("AttachedFiles", attachedFiles);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/AttachedFile";
    }

    @RequestMapping(value = "sendEmail", method = RequestMethod.GET)
    public String sendEmail(Model m) {
        /**
         * "승인대기", "발행완료" 상태의 전자명세서와 관련된 발행 안내 메일을 재전송 합니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#SendEmail
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 수신자 이메일주소
        String receiver = "test@test.com";

        try {
            Response response = statementService.sendEmail(CorpNum, itemCode, mgtKey, receiver);
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
         * 전자명세서와 관련된 안내 SMS(단문) 문자를 재전송하는 함수로, 팝빌 사이트 [문자·팩스] > [문자] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다.
         * - 메시지는 최대 90byte까지 입력 가능하고, 초과한 내용은 자동으로 삭제되어 전송합니다. (한글 최대 45자)
         * - 함수 호출시 포인트가 과금됩니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#SendSMS
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 발신번호
        String sender = "07043042991";

        // 수신번호
        String receiver = "010111222";

        // 문자 전송 내용 (90Byte 초과시 길이가 조정되어 전송)
        String contents = "전자명세서 문자메시지 전송 테스트입니다.";

        try {
            Response response = statementService.sendSMS(CorpNum, itemCode, mgtKey, sender, receiver, contents);
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
         * 전자명세서를 팩스로 전송하는 함수로, 팝빌 사이트 [문자·팩스] > [팩스] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다.
         * - 함수 호출시 포인트가 과금됩니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#SendFAX
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 발신자 번호
        String sender = "07043042991";

        // 수신자 팩스번호
        String receiver = "070111222";

        try {
            Response response = statementService.sendFAX(CorpNum, itemCode, mgtKey, sender, receiver);
            m.addAttribute("Response", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "FAXSend", method = RequestMethod.GET)
    public String FAXSend(Model m) {
        /**
         * 전자명세서를 팩스로 전송하는 함수로, 팝빌에 데이터를 저장하는 과정이 없습니다.
         * - 팝빌 사이트 [문자·팩스] > [팩스] > [전송내역] 메뉴에서 전송결과를 확인 할 수 있습니다.
         * - 함수 호출시 포인트가 과금됩니다.
         * - 팩스 발행 요청시 작성한 문서번호는 팩스전송 파일명으로 사용됩니다.
         * - 팩스 전송결과를 확인하기 위해서는 선팩스 전송 요청 시 반환받은 접수번호를 이용하여 팩스 API의 전송결과 확인 (GetFaxDetail API) 함수를 이용하면 됩니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#FAXSend
         */

        // 전자명세서 정보 객체
        Statement statement = new Statement();

        // 명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        statement.setItemCode((short) 121);

        // 문서번호, 1~24자리 (숫자, 영문, '-', '_') 조합으로 사업자 별로 중복되지 않도록 구성
        statement.setMgtKey("20230102-BOOT003");

        // 맞춤양식코드, 미기재시 기본양식으로 처리
        statement.setFormCode("");

        // 작성일자, 형태 yyyyMmdd
        statement.setWriteDate("20220218");

        // {과세, 영세, 면세} 중 기재
        statement.setTaxType("과세");

        // {영수, 청구, 없음} 중 기재
        statement.setPurposeType("영수");

        // 기재상 일련번호 항목
        statement.setSerialNum("123");

        // 세액 합계
        statement.setTaxTotal("40000");

        // 공급가액 합계
        statement.setSupplyCostTotal("400000");

        // 합계금액. 공급가액 + 세액
        statement.setTotalAmount("440000");

        // 기재상 비고 항목
        statement.setRemark1("비고1");
        statement.setRemark2("비고2");
        statement.setRemark3("비고3");

        /*********************************************************************
         *                                발신자 정보
         *********************************************************************/

        // 발신자 사업자번호
        statement.setSenderCorpNum("1234567890");

        // 발신자 종사업장 식별번호, 숫자 4자리, 필요시 기재
        statement.setSenderTaxRegID("");

        // 발신자 상호
        statement.setSenderCorpName("발신자 상호");

        // 발신자 대표자 성명
        statement.setSenderCEOName("발신자 대표자 성명");

        // 발신자 주소
        statement.setSenderAddr("발신자 주소");

        // 발신자 업태
        statement.setSenderBizType("업태");

        // 발신자 종목
        statement.setSenderBizClass("업종");

        // 발신자 담당자 성명
        statement.setSenderContactName("발신자 담당자 성명");

        // 발신자 담당자 연락처
        statement.setSenderTEL("070-7070-0707");

        // 발신자 담당자 휴대폰번호
        statement.setSenderHP("010-000-2222");

        // 발신자 담당자 메일주소
        statement.setSenderEmail("test@test.com");

        /*********************************************************************
         *                            수신자 정보
         *********************************************************************/

        // 수신자 사업자번호
        statement.setReceiverCorpNum("8888888888");

        // 수신자 상호
        statement.setReceiverCorpName("수신자 상호");

        // 수신자 대표자 성명
        statement.setReceiverCEOName("수신자 대표자 성명");

        // 수신자 주소
        statement.setReceiverAddr("수신자 주소");

        // 수신자 업태
        statement.setReceiverBizType("수신자 업태");

        // 수신자 종목
        statement.setReceiverBizClass("수신자 종목");

        // 수신자 담당자 성명
        statement.setReceiverContactName("수신자 담당자 성명");

        // 수신자 메일주소
        // 팝빌 개발환경에서 테스트하는 경우에도 안내 메일이 전송되므로,
        // 실제 거래처의 메일주소가 기재되지 않도록 주의
        statement.setReceiverEmail("test@receiver.com");

        Map<String, String> propertyBag = new HashMap<String, String>();

        propertyBag.put("Balance", "15000");            // 전잔액
        propertyBag.put("Deposit", "5000");             // 입금액
        propertyBag.put("CBalance", "20000");           // 현잔액

        statement.setPropertyBag(propertyBag);

        // 사업자등록증 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBusinessLicenseYN(false);

        // 통장사본 첨부 여부 (true / false 중 택 1)
        // └ true = 첨부 , false = 미첨부(기본값)
        // - 팝빌 사이트 또는 인감 및 첨부문서 등록 팝업 URL (GetSealURL API) 함수를 이용하여 등록
        statement.setBankBookYN(false);

        /*********************************************************************
         *                            전자명세서 품목항목
         *********************************************************************/

        statement.setDetailList(new ArrayList<StatementDetail>());

        StatementDetail detail = new StatementDetail();    // 상세항목(품목) 배열

        detail.setSerialNum((short) 1);                    // 일련번호, 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                        // 품목명
        detail.setQty("1");                                // 수량
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액

        statement.getDetailList().add(detail);

        detail = new StatementDetail();                    // 상세항목(품목) 배열
        detail.setSerialNum((short) 2);                    // 일련번호 1부터 순차기재
        detail.setPurchaseDT("20230102");                  // 거래일자
        detail.setItemName("품명");                        // 품목명
        detail.setQty("1");                                // 수량
        detail.setSupplyCost("200000");                    // 공급가액
        detail.setTax("20000");                            // 세액

        statement.getDetailList().add(detail);

        // 팩스전송 발신번호
        String sendNum = "07043042991";

        // 수신팩스번호
        String receiveNum = "00111222";

        try {
            String receiptNum = statementService.FAXSend(CorpNum, statement, sendNum, receiveNum);
            m.addAttribute("Result", receiptNum);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "attachStatement", method = RequestMethod.GET)
    public String attachStatement(Model m) {
        /**
         * 하나의 전자명세서에 다른 전자명세서를 첨부합니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#AttachStatement
         */

        // 전자명세서 유형 코드
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 첨부할 전자명세서 유형 코드
        int subItemCode = 121;

        // 첨부할 전자명세서 문서번호
        String subMgtKey = "20230102-BOOT002";

        try {
            Response response = statementService.attachStatement(CorpNum, itemCode, mgtKey, subItemCode, subMgtKey);
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
         * 하나의 전자명세서에 첨부된 다른 전자명세서를 해제합니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#DetachStatement
         */

        // 전자명세서 유형 코드
        int itemCode = 121;

        // 전자명세서 문서번호
        String mgtKey = "20230102-BOOT001";

        // 첨부해제할 전자명세서 유형 코드
        int subItemCode = 121;

        // 첨부해제할 전자명세서 문서번호
        String subMgtKey = "20220218-BOOT002";

        try {
            Response response = statementService.detachStatement(CorpNum, itemCode, mgtKey, subItemCode, subMgtKey);
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
         * 전자명세서 관련 메일 항목에 대한 발송설정을 확인합니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#ListEmailConfig
         */
        Map<String, Boolean> emailSendConfigs = new HashMap<String, Boolean>();

        try {
            EmailSendConfig[] Configs = statementService.listEmailConfig(CorpNum);

            for(EmailSendConfig emailSendConfig : Configs) {
                emailSendConfigs.put(emailSendConfig.getEmailType(), emailSendConfig.getSendYN());
            }

            m.addAttribute("EmailSendConfigs", emailSendConfigs);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Statement/EmailSendConfig";
    }

    @RequestMapping(value = "updateEmailConfig", method = RequestMethod.GET)
    public String updateEmailConfig(Model m) {
        /**
         * 전자명세서 관련 메일 항목에 대한 발송설정을 수정합니다.
         * - https://developers.popbill.com/reference/statement/java/api/etc#UpdateEmailConfig
         *
         * 메일전송유형
         * - SMT_ISSUE : 수신자에게 전자명세서가 발행 되었음을 알려주는 메일입니다.
         * - SMT_ACCEPT : 발신자에게 전자명세서가 승인 되었음을 알려주는 메일입니다.
         * - SMT_DENY : 발신자에게 전자명세서가 거부 되었음을 알려주는 메일입니다.
         * - SMT_CANCEL : 수신자에게 전자명세서가 취소 되었음을 알려주는 메일입니다.
         * - SMT_CANCEL_ISSUE : 수신자에게 전자명세서가 발행취소 되었음을 알려주는 메일입니다.
         */

        // 메일 전송 유형
        String emailType = "SMT_ISSUE";

        // 전송 여부 (true = 전송, false = 미전송)
        Boolean sendYN = true;

        try {
            Response response = statementService.updateEmailConfig(CorpNum, emailType, sendYN);
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
         * 전자명세서 발행시 과금되는 포인트 단가를 확인합니다.
         * - https://developers.popbill.com/reference/statement/java/common-api/point#GetUnitCost
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        try {
            float unitCost = statementService.getUnitCost(CorpNum, itemCode);
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
         * 팝빌 전자명세서 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/statement/java/common-api/point#GetChargeInfo
         */

        // 전자명세서 유형 코드, [121 - 거래명세서], [122 - 청구서], [123 - 견적서], [124 - 발주서], [125 - 입금표], [126 - 영수증]
        int itemCode = 121;

        try {
            ChargeInfo chrgInfo = statementService.getChargeInfo(CorpNum, itemCode);
            m.addAttribute("ChargeInfo", chrgInfo);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }
}
