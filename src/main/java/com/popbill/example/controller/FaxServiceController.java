package com.popbill.example.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.ChargeInfo;
import com.popbill.api.FaxService;
import com.popbill.api.FaxUploadFile;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.fax.FAXSearchResult;
import com.popbill.api.fax.FaxResult;
import com.popbill.api.fax.Receiver;
import com.popbill.api.fax.SenderNumber;

@Controller
@RequestMapping("FaxService")
public class FaxServiceController {

    @Autowired
    private FaxService faxService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Fax/index";
    }

    @RequestMapping(value = "getSenderNumberMgtURL", method = RequestMethod.GET)
    public String getSenderNumberMgtURL(Model m) {
        /*
         * 발신번호를 등록하고 내역을 확인하는 팩스 발신번호 관리 페이지 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/fax/java/api#GetSenderNumberMgtURL
         */
        try {

            String url = faxService.getSenderNumberMgtURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getSenderNumberList", method = RequestMethod.GET)
    public String getSenderNumberList(Model m) {
        /*
         * 팝빌에 등록한 연동회원의 팩스 발신번호 목록을 확인합니다. 
         * - https://docs.popbill.com/fax/java/api#GetSenderNumberList
         */

        try {
            SenderNumber[] senderNumberList = faxService.getSenderNumberList(testCorpNum);
            m.addAttribute("SenderNumberList", senderNumberList);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Fax/SenderNumber";
    }

    @RequestMapping(value = "sendFAX", method = RequestMethod.GET)
    public String sendFAX(Model m) throws URISyntaxException {
        /*
         * 팩스 1건을 전송합니다. (최대 전송파일 개수: 20개) 
         * - 팩스전송 문서 파일포맷 안내 : https://docs.popbill.com/fax/format?lang=java 
         * - https://docs.popbill.com/fax/java/api#SendFAX
         */

        // 발신번호
        String sendNum = "07043042995";

        // 수신번호
        String receiveNum = "010111222";

        // 수신자명
        String receiveName = "수신자 명칭";

        File[] files = new File[2];
        try {
            // 파일 전송 개수 최대 20개
            files[0] = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
            files[1] = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
        } catch (URISyntaxException e1) {
            throw e1;
        }

        // 전송 예약일시
        Date reserveDT = null;

        // 광고팩스 전송여부
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 제목";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "abc";

        try {

            String receiptNum = faxService.sendFAX(testCorpNum, sendNum, receiveNum, receiveName, files, reserveDT,
                    testUserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFAX_Multi", method = RequestMethod.GET)
    public String sendFAX_Multi(Model m) throws URISyntaxException {
        /*
         * 동일한 팩스파일을 다수의 수신자에게 전송하기 위해 팝빌에 접수합니다. (최대 전송파일 개수 : 20개) (최대 1,000건) 
         * - 팩스전송 문서 파일포맷 안내 : https://docs.popbill.com/fax/format?lang=java 
         * - https://docs.popbill.com/fax/java/api#SendFAX_Multi
         */

        // 발신번호
        String sendNum = "07043042991";

        // 수신자 정보 (최대 1000건)
        Receiver[] receivers = new Receiver[2];

        Receiver receiver1 = new Receiver();
        receiver1.setReceiveName("수신자1"); // 수신자명
        receiver1.setReceiveNum("010111222"); // 수신팩스번호
        receivers[0] = receiver1;

        Receiver receiver2 = new Receiver();
        receiver2.setReceiveName("수신자2"); // 수신자명
        receiver2.setReceiveNum("010333444"); // 수신팩스번호
        receivers[1] = receiver1;

        File[] files = new File[2];
        try {
            // 파일 전송 개수 최대 20개
            files[0] = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
            files[1] = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
        } catch (URISyntaxException e1) {
            throw e1;
        }

        // 전송예약일시
        Date reserveDT = null;

        // 광고팩스 전송여부
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 동보전송 제목";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.sendFAX(testCorpNum, sendNum, receivers, files, reserveDT, testUserID, adsYN,
                    title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFAXBinary", method = RequestMethod.GET)
    public String sendFAXBinary(Model m) throws URISyntaxException {
        /*
         * 전송할 파일의 바이너리 데이터를 팩스 1건 전송합니다. (최대 전송파일 개수: 20개) 
         * - 팩스전송 문서 파일포맷 안내 : https://docs.popbill.com/fax/format?lang=java 
         * - https://docs.popbill.com/fax/java/api#SendFAXBinary
         */

        // 발신번호
        String sendNum = "07043042995";

        // 수신번호
        String receiveNum = "010111222";

        // 수신자명
        String receiveName = "수신자 명칭";

        File file = new File("/Users/John/Desktop/test.pdf");
        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // 파일정보 배열, 최대 20개까지 입력가능.
        FaxUploadFile[] fileList = new FaxUploadFile[1];
        FaxUploadFile uf = new FaxUploadFile();

        // 파일명
        uf.fileName = "test.pdf";

        // 파일 InputStream
        uf.fileData = targetStream;

        fileList[0] = uf;

        // 전송 예약일시
        Date reserveDT = null;

        // 광고팩스 전송여부
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 제목";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.sendFAXBinary(testCorpNum, sendNum, receiveNum, receiveName, fileList,
                    reserveDT, testUserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFAXBinary_Multi", method = RequestMethod.GET)
    public String sendFAXBinary_Multi(Model m) throws URISyntaxException {
        /*
         * 동일한 파일의 바이너리 데이터를 다수의 수신자에게 전송하기 위해 팝빌에 접수합니다. (최대 전송파일 개수 : 20개) (최대 1,000건) 
         * - 팩스전송 문서 파일포맷 안내 : https://docs.popbill.com/fax/format?lang=java 
         * - https://docs.popbill.com/fax/java/api#SendFAXBinary_multi
         */

        // 발신번호
        String sendNum = "07043042991";

        // 수신자 정보 (최대 1000건)
        Receiver[] receivers = new Receiver[2];

        Receiver receiver1 = new Receiver();
        receiver1.setReceiveName("수신자1"); // 수신자명
        receiver1.setReceiveNum("010111222"); // 수신팩스번호
        receivers[0] = receiver1;

        Receiver receiver2 = new Receiver();
        receiver2.setReceiveName("수신자2"); // 수신자명
        receiver2.setReceiveNum("010333444"); // 수신팩스번호
        receivers[1] = receiver1;

        File file = new File("/Users/John/Desktop/test.pdf");
        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // 파일정보 배열, 최대 20개까지 입력가능.
        FaxUploadFile[] fileList = new FaxUploadFile[1];
        FaxUploadFile uf = new FaxUploadFile();

        // 파일명
        uf.fileName = "test.pdf";

        // 파일 InputStream
        uf.fileData = targetStream;

        fileList[0] = uf;

        // 전송예약일시
        Date reserveDT = null;

        // 광고팩스 전송여부
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 동보전송 제목";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.sendFAXBinary(testCorpNum, sendNum, receivers, fileList, reserveDT,
                    testUserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAX", method = RequestMethod.GET)
    public String resendFAX(Model m) {
        /*
         * 팝빌에서 반환받은 접수번호를 통해 팩스 1건을 재전송합니다. 
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다. 
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리) 
         * - https://docs.popbill.com/fax/java/api#ResendFAX
         */

        // 원본 팩스 접수번호
        String orgReceiptNum = "021112319124600001";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 수신번호/수신자명 모두 공백처리시 기존전송정보로 재전송
        // 수신번호
        String receiveNum = "";

        // 수신자명
        String receiveName = "";

        // 전송 예약일시
        Date reserveDT = null;

        // 팩스 제목
        String title = "팩스 재전송 제목";

        // 재전송 팩스의 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        // 재전송 팩스의 전송상태확인(getFaxResultRN) / 예약전송취소(cancelReserveRN) 에 이용됩니다.
        String requestNum = "";

        try {

            String receiptNum = faxService.resendFAX(testCorpNum, orgReceiptNum, sendNum, sendName, receiveNum,
                    receiveName, reserveDT, testUserID, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAX_Multi", method = RequestMethod.GET)
    public String resendFAX_Multi(Model m) {
        /*
         * 동일한 팩스파일을 다수의 수신자에게 전송하기 위해 팝빌에 접수합니다. (최대 전송파일 개수: 20개) (최대 1,000건) 
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다. 
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리) 
         * - https://docs.popbill.com/fax/java/api#ResendFAX_Multi
         */

        // 원본 팩스 접수번호
        String orgReceiptNum = "021112319124600001";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 팩스수신정보를 기존전송정보와 동일하게 재전송하는 경우, receivers 변수 null 처리
        Receiver[] receivers = null;

//      팩스수신정보를 기존전송정보와 다르게 재전송하는 경우, 아래의 코드 적용 (최대 1000건)
//      Receiver[] receivers = new Receiver[2];

//      Receiver receiver1 = new Receiver();
//      receiver1.setReceiveName("수신자1");       // 수신자명
//      receiver1.setReceiveNum("010111222");   // 수신팩스번호
//      receivers[0] = receiver1;

//      Receiver receiver2 = new Receiver();
//      receiver2.setReceiveName("수신자2");       // 수신자명
//      receiver2.setReceiveNum("010333444");   // 수신팩스번호
//      receivers[1] = receiver1;

        // 전송 예약일시
        Date reserveDT = null;

        // 팩스제목
        String title = "팩스 재전송(동보) 제목";

        // 재전송 팩스의 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        // 재전송 팩스의 전송상태확인(getFaxResultRN) / 예약전송취소(cancelReserveRN) 에 이용됩니다.
        String requestNum = "";

        try {

            String receiptNum = faxService.resendFAX(testCorpNum, orgReceiptNum, sendNum, sendName, receivers,
                    reserveDT, testUserID, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAXRN", method = RequestMethod.GET)
    public String resendFAXRN(Model m) {
        /*
         * 파트너가 할당한 전송요청 번호를 통해 팩스 1건을 재전송합니다. 
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다. 
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리) 
         * - https://docs.popbill.com/fax/java/api#ResendFAXRN
         */

        // 재전송 팩스의 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        // 재전송 팩스의 전송상태확인(getFaxResultRN) / 예약전송취소(cancelReserveRN) 에 이용됩니다.
        String requestNum = "";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 수신번호/수신자명 모두 공백처리시 기존전송정보로 재전송
        // 수신번호
        String receiveNum = "";

        // 수신자명
        String receiveName = "";

        // 전송 예약일시
        Date reserveDT = null;

        // 팩스 제목
        String title = "팩스 재전송 제목";

        // 원본 팩스 전송시 할당한 전송요청번호(requestNum)
        String orgRequestNum = "20210701-001";

        try {

            String receiptNum = faxService.resendFAXRN(testCorpNum, requestNum, sendNum, sendName, receiveNum,
                    receiveName, reserveDT, testUserID, title, orgRequestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAXRN_Multi", method = RequestMethod.GET)
    public String resendFAXRN_Multi(Model m) {
        /*
         * 파트너가 할당한 전송요청 번호를 통해 다수건의 팩스를 재전송합니다. (최대 전송파일 개수: 20개) (최대 1,000건) 
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다. 
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리)
         * - https://docs.popbill.com/fax/java/api#ResendFAXRN_Multi
         */

        // 재전송 팩스의 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        // 재전송 팩스의 전송상태확인(getFaxResultRN) / 예약전송취소(cancelReserveRN) 에 이용됩니다.
        String requestNum = "";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 팩스수신정보를 기존전송정보와 동일하게 재전송하는 경우, receivers 변수 null 처리
        Receiver[] receivers = null;

//      팩스수신정보를 기존전송정보와 다르게 재전송하는 경우, 아래의 코드 적용 (최대 1000건)
//      Receiver[] receivers = new Receiver[2];

//      Receiver receiver1 = new Receiver();
//      receiver1.setReceiveName("수신자1");       // 수신자명
//      receiver1.setReceiveNum("010111222");   // 수신팩스번호
//      receivers[0] = receiver1;

//      Receiver receiver2 = new Receiver();
//      receiver2.setReceiveName("수신자2");       // 수신자명
//      receiver2.setReceiveNum("010333444");   // 수신팩스번호
//      receivers[1] = receiver1;

        // 전송 예약일시
        Date reserveDT = null;

        // 팩스제목
        String title = "팩스 재전송(동보) 제목";

        // 원본 팩스 전송시 할당한 전송요청번호(requestNum)
        String orgRequestNum = "20210701-001";

        try {

            String receiptNum = faxService.resendFAXRN(testCorpNum, requestNum, sendNum, sendName, receivers, reserveDT,
                    testUserID, title, orgRequestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "cancelReserve", method = RequestMethod.GET)
    public String cancelReserve(Model m) {
        /*
         * 팝빌에서 반환받은 접수번호를 통해 예약접수된 팩스 전송을 취소합니다. (예약시간 10분 전까지 가능) 
         * - https://docs.popbill.com/fax/java/api#CancelReserve
         */

        // 전송요청(sendFAX)시 발급받은 팩스접수번호
        String receiptNum = "021010414344900001";

        try {
            Response response = faxService.cancelReserve(testCorpNum, receiptNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "cancelReserveRN", method = RequestMethod.GET)
    public String cancelReserveRN(Model m) {
        /*
         * 파트너가 할당한 전송요청 번호를 통해 예약접수된 팩스 전송을 취소합니다. (예약시간 10분 전까지 가능) 
         * - https://docs.popbill.com/fax/java/api#CancelReserveRN
         */

        // 예약팩스전송 요청시 할당한 전송요청번호
        String requestNum = "20210104-001";

        try {
            Response response = faxService.cancelReserveRN(testCorpNum, requestNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getFaxResult", method = RequestMethod.GET)
    public String getFaxResult(Model m) {
        /*
         * 팝빌에서 반환 받은 접수번호를 통해 팩스 전송상태 및 결과를 확인합니다. 
         * - https://docs.popbill.com/fax/java/api#GetFaxResult
         */

        // 전송요청(sendFAX)시 발급받은 접수번호
        String receiptNum = "021111909011900001";

        try {
            FaxResult[] faxResults = faxService.getFaxResult(testCorpNum, receiptNum);

            m.addAttribute("FaxResults", faxResults);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Fax/FaxResult";
    }

    @RequestMapping(value = "getFaxResultRN", method = RequestMethod.GET)
    public String getFaxResultRN(Model m) {
        /*
         * 파트너가 할당한 전송요청 번호를 통해 팩스 전송상태 및 결과를 확인합니다. 
         * - https://docs.popbill.com/fax/java/api#GetFaxResultRN
         */

        // 팩스전송 요청시 할당한 전송요청번호
        String requestNum = "20210701-0011";

        try {
            FaxResult[] faxResults = faxService.getFaxResultRN(testCorpNum, requestNum);

            m.addAttribute("FaxResults", faxResults);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Fax/FaxResult";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /*
         * 검색조건에 해당하는 팩스 전송내역 목록을 조회합니다. (조회기간 단위 : 최대 2개월) 
         * - 팩스 접수일시로부터 2개월 이내 접수건만 조회할 수 있습니다. 
         * - https://docs.popbill.com/fax/java/api#Search
         */

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20211201";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20211227";

        // 전송상태 배열, 1-대기, 2-성공, 3-실패, 4-취소
        String[] State = { "1", "2", "3", "4" };

        // 예약여부, false-전체조회, true-예약전송건 조회
        Boolean ReserveYN = false;

        // 개인조회 여부, false- 전체조회, true-개인조회
        Boolean SenderOnly = false;

        // 페이지 번호
        int Page = 1;

        // 페이지당 목록개수 (최대 1000건)
        int PerPage = 1000;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        // 조회 검색어.
        // 팩스 전송시 입력한 발신자명 또는 수신자명 기재.
        // 조회 검색어를 포함한 발신자명 또는 수신자명을 검색합니다.
        String QString = "";

        try {

            FAXSearchResult response = faxService.search(testCorpNum, SDate, EDate, State, ReserveYN, SenderOnly, Page,
                    PerPage, Order, QString);

            m.addAttribute("SearchResult", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Fax/SearchResult";
    }

    @RequestMapping(value = "getSentListURL", method = RequestMethod.GET)
    public String getSentListURL(Model m) {
        /*
         * 팝빌 사이트와 동일한 팩스 전송내역 확인 페이지의 팝업 URL을 반환합니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/fax/java/api#GetSentListURL
         */
        try {

            String url = faxService.getSentListURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPreviewURL", method = RequestMethod.GET)
    public String getPrevewURL(Model m) {
        /*
         * 팩스 미리보기 팝업 URL을 반환하며, 팩스전송을 위한 TIF 포맷 변환 완료 후 호출 할 수 있습니다. 
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다. 
         * - https://docs.popbill.com/fax/java/api#GetPreviewURL
         */
        try {

            // 팩스 접수번호
            String receiptNum = "021010414350900001";

            String url = faxService.getPreviewURL(testCorpNum, receiptNum);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /*
         * 팩스 전송시 과금되는 포인트 단가를 확인합니다. 
         * - https://docs.popbill.com/fax/java/api#GetUnitCost
         */

        try {

            float unitCost = faxService.getUnitCost(testCorpNum);

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
         * 팝빌 팩스 API 서비스 과금정보를 확인합니다. 
         * - https://docs.popbill.com/fax/java/api#GetChargeInfo
         */

        try {

            ChargeInfo chrgInfo = faxService.getChargeInfo(testCorpNum);

            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }
}
