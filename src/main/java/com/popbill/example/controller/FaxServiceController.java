package com.popbill.example.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Locale;
import javax.sound.midi.Receiver;
import com.popbill.api.ChargeInfo;
import com.popbill.api.FaxService;
import com.popbill.api.FaxUploadFile;
import com.popbill.api.PopbillException;
import com.popbill.api.fax.FAXSearchResult;
import com.popbill.api.fax.FaxResult;
import com.popbill.api.fax.SenderNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("FaxService")
public class FaxServiceController {

    @Autowired
    private FaxService faxService;

    // 팝빌회원 사업자번호
    private String CorpNum = "1234567890";

    // 팝빌회원 아이디
    private String UserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Fax/index";
    }

    @RequestMapping(value = "checkSenderNumber", method = RequestMethod.GET)
    public String checkSenderNumber(Model m) {
        /**
         * 팩스 발신번호 등록여부를 확인합니다.
         * - https://developers.popbill.com/reference/fax/java/api/sendnum#CheckSenderNumber
         */

        // 확인할 발신번호
        String Sender = "070-4304-2991";

        try {

            Response response = faxService.checkSenderNumber(CorpNum, Sender);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getSenderNumberMgtURL", method = RequestMethod.GET)
    public String getSenderNumberMgtURL(Model m) {
        /**
         * 발신번호를 등록하고 내역을 확인하는 팩스 발신번호 관리 페이지 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/sendnum#GetSenderNumberMgtURL
         */
        try {

            String url = faxService.getSenderNumberMgtURL(CorpNum, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getSenderNumberList", method = RequestMethod.GET)
    public String getSenderNumberList(Model m) {
        /**
         * 팝빌에 등록한 연동회원의 팩스 발신번호 목록을 확인합니다.
         * - https://developers.popbill.com/reference/fax/java/api/sendnum#GetSenderNumberList
         */

        try {
            SenderNumber[] senderNumberList = faxService.getSenderNumberList(CorpNum);
            m.addAttribute("SenderNumberList", senderNumberList);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Fax/SenderNumber";
    }

    @RequestMapping(value = "sendFAX", method = RequestMethod.GET)
    public String sendFAX(Model m) throws URISyntaxException {
        /**
         * 팩스 1건을 전송합니다. (최대 전송파일 개수: 20개)
         * - https://developers.popbill.com/reference/fax/java/api/send#SendFAX
         */

        // 발신번호
        // 팝빌에 등록되지 않은 번호를 입력하는 경우 '원발신번호'로 팩스 전송됨
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

        // 전송예약일시, null인 경우 즉시전송
        Date reserveDT = null;
        // SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        // try {
        //    reserveDT = format.parse("20220201140000");
        // } catch (ParseException e) {
        //    e.printStackTrace();
        // }

        // 광고팩스 전송여부
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 제목";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.sendFAX(CorpNum, sendNum, receiveNum, receiveName, files, reserveDT,
                    UserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFAX_Multi", method = RequestMethod.GET)
    public String sendFAX_Multi(Model m) throws URISyntaxException {
        /**
         * 동일한 팩스파일을 다수의 수신자에게 전송하기 위해 팝빌에 접수합니다. (최대 전송파일 개수 : 20개) (최대 1,000건)
         * - https://developers.popbill.com/reference/fax/java/api/send#SendFAXMulti
         */

        // 발신번호
        // 팝빌에 등록되지 않은 번호를 입력하는 경우 '원발신번호'로 팩스 전송됨
        String sendNum = "07043042991";

        // 수신자 정보 배열 (최대 1000건)
        Receiver[] receivers = new Receiver[2];

        Receiver receiver1 = new Receiver();
        receiver1.setReceiveName("수신자1");        // 수신자명
        receiver1.setReceiveNum("010111222");     // 수신팩스번호
        receiver1.setInterOPRefKey("20221006-FAX001");  // 파트너 지정키
        receivers[0] = receiver1;

        Receiver receiver2 = new Receiver();
        receiver2.setReceiveName("수신자2");        // 수신자명
        receiver2.setReceiveNum("010333444");     // 수신팩스번호
        receiver2.setInterOPRefKey("20221006-FAX002");  // 파트너 지정키
        receivers[1] = receiver2;

        File[] files = new File[2];
        try {
            // 파일 전송 개수 최대 20개
            files[0] = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
            files[1] = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
        } catch (URISyntaxException e1) {
            throw e1;
        }

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 광고팩스 전송여부 , true / false 중 택 1
        // └ true = 광고 , false = 일반
        // └ 미입력 시 기본값 false 처리
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 동보전송 제목";

        // 전송요청번호
        // 팝빌이 접수 단위를 식별할 수 있도록 파트너가 부여하는 식별번호.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.sendFAX(CorpNum, sendNum, receivers,
                    files, reserveDT, UserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }


    @RequestMapping(value = "sendFAXBinary", method = RequestMethod.GET)
    public String sendFAXBinary(Model m) throws URISyntaxException {
        /**
         * 전송할 파일의 바이너리 데이터를 팩스 1건 전송합니다. (최대 전송파일 개수: 20개)
         * - https://developers.popbill.com/reference/fax/java/api/send#SendFAXBinary
         */

        // 발신번호
        // 팝빌에 등록되지 않은 번호를 입력하는 경우 '원발신번호'로 팩스 전송됨
        String sendNum = "07043042995";

        // 수신번호
        String receiveNum = "010111222";

        // 수신자명
        String receiveName = "수신자 명칭";

        // 전송할 File InputStream 생성을 위한 샘플코드.
        File file = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
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

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 광고팩스 전송여부 , true / false 중 택 1
        // └ true = 광고 , false = 일반
        // └ 미입력 시 기본값 false 처리
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 제목";

        // 전송요청번호
        // 팝빌이 접수 단위를 식별할 수 있도록 파트너가 부여하는 식별번호.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.sendFAXBinary(CorpNum, sendNum, receiveNum,
                    receiveName, fileList, reserveDT, UserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFAXBinary_Multi", method = RequestMethod.GET)
    public String sendFAXBinary_Multi(Model m) throws URISyntaxException {
        /**
         * 동일한 파일의 바이너리 데이터를 다수의 수신자에게 전송하기 위해 팝빌에 접수합니다. (최대 전송파일 개수 : 20개) (최대 1,000건)
         * - https://developers.popbill.com/reference/fax/java/api/send#SendFAXBinaryMulti
         */

        // 발신번호
        // 팝빌에 등록되지 않은 번호를 입력하는 경우 '원발신번호'로 팩스 전송됨
        String sendNum = "07043042991";

        // 수신자 정보 (최대 1000건)
        Receiver[] receivers = new Receiver[2];

        Receiver receiver1 = new Receiver();
        receiver1.setReceiveName("수신자1");        // 수신자명
        receiver1.setReceiveNum("010111222");     // 수신팩스번호
        receiver1.setInterOPRefKey("20221006-FAXBinary01");  // 파트너 지정키
        receivers[0] = receiver1;

        Receiver receiver2 = new Receiver();
        receiver2.setReceiveName("수신자2");        // 수신자명
        receiver2.setReceiveNum("010333444");     // 수신팩스번호
        receiver2.setInterOPRefKey("20221006-FAXBinary02");  // 파트너 지정키
        receivers[1] = receiver2;

        // 전송할 File InputStream 생성을 위한 샘플코드.
        File file = new File(getClass().getClassLoader().getResource("static/image/nonbg_statement.pdf").toURI());
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

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 광고팩스 전송여부 , true / false 중 택 1
        // └ true = 광고 , false = 일반
        // └ 미입력 시 기본값 false 처리
        Boolean adsYN = false;

        // 팩스제목
        String title = "팩스 동보전송 제목";

        // 전송요청번호
        // 팝빌이 접수 단위를 식별할 수 있도록 파트너가 부여하는 식별번호.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "20221012-request";

        try {

            String receiptNum = faxService.sendFAXBinary(CorpNum, sendNum, receivers,
                    fileList, reserveDT, UserID, adsYN, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }



    @RequestMapping(value = "resendFAX", method = RequestMethod.GET)
    public String resendFAX(Model m) {
        /**
         * 팝빌에서 반환받은 접수번호를 통해 팩스 1건을 재전송합니다.
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다.
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리)
         * - 변환실패 사유로 전송실패한 팩스 접수건은 재전송이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/send#ResendFAX
         */

        // 원본 팩스 접수번호
        String orgReceiptNum = "022021803102600001";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 수신번호, 공백처리시 기존전송정보로 재전송
        String receiveNum = "";

        // 수신자명, 공백처리시 기존전송정보로 재전송
        String receiveName = "";

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 팩스 제목
        String title = "팩스 재전송 제목";

        // 재전송 팩스의 전송요청번호
        // 팝빌이 접수 단위를 식별할 수 있도록 파트너가 부여하는 식별번호.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.resendFAX(CorpNum, orgReceiptNum, sendNum,
                    sendName, receiveNum, receiveName, reserveDT, UserID, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAX_Multi", method = RequestMethod.GET)
    public String resendFAX_Multi(Model m) {
        /**
         * 동일한 팩스파일을 다수의 수신자에게 전송하기 위해 팝빌에 접수합니다. (최대 전송파일 개수: 20개) (최대 1,000건)
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다.
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리)
         * - 변환실패 사유로 전송실패한 팩스 접수건은 재전송이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/send#ResendFAXMulti
         */

        // 원본 팩스 접수번호
        String orgReceiptNum = "022100616261900001";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 팩스수신정보를 기존전송정보와 동일하게 재전송하는 경우, receivers 변수 null 처리
        Receiver[] receivers = null;

        // 팩스수신정보를 기존전송정보와 다르게 재전송하는 경우, 아래의 코드 적용 (최대 1000건)
        // Receiver[] receivers = new Receiver[2];

        // Receiver receiver1 = new Receiver();
        // receiver1.setReceiveName("수신자1");      // 수신자명
        // receiver1.setReceiveNum("010111222");     // 수신팩스번호
        // receiver1.setInterOPRefKey("20221006-reFAX01");  // 파트너 지정키
        // receivers[0] = receiver1;

        // Receiver receiver2 = new Receiver();
        // receiver2.setReceiveName("수신자2");      // 수신자명
        // receiver2.setReceiveNum("010333444");     // 수신팩스번호
        // receiver2.setInterOPRefKey("20221006-reFAX02");  // 파트너 지정키
        // receivers[1] = receiver2;

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 팩스제목
        String title = "팩스 재전송(동보) 제목";

        // 재전송 팩스의 전송요청번호
        // 팝빌이 접수 단위를 식별할 수 있도록 파트너가 부여하는 식별번호.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = faxService.resendFAX(CorpNum, orgReceiptNum, sendNum,
                    sendName, receivers, reserveDT, UserID, title, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAXRN", method = RequestMethod.GET)
    public String resendFAXRN(Model m) {
        /**
         * 파트너가 할당한 전송요청 번호를 통해 팩스 1건을 재전송합니다.
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다.
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리)
         * - 변환실패 사유로 전송실패한 팩스 접수건은 재전송이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/send#ResendFAXRN
         */

        // 재전송 팩스의 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 수신번호, 공백처리시 기존전송정보로 재전송
        String receiveNum = "";

        // 수신자명, 공백처리시 기존전송정보로 재전송
        String receiveName = "";

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 팩스 제목
        String title = "팩스 재전송 제목";

        // 원본 팩스 전송시 파트너가 할당한 전송요청번호(requestNum)
        String orgRequestNum = "";

        try {

            String receiptNum = faxService.resendFAXRN(CorpNum, requestNum, sendNum,
                    sendName, receiveNum, receiveName, reserveDT, UserID, title, orgRequestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "resendFAXRN_Multi", method = RequestMethod.GET)
    public String resendFAXRN_Multi(Model m) {
        /**
         * 파트너가 할당한 전송요청 번호를 통해 다수건의 팩스를 재전송합니다. (최대 전송파일 개수: 20개) (최대 1,000건)
         * - 발신/수신 정보 미입력시 기존과 동일한 정보로 팩스가 전송되고, 접수일 기준 최대 60일이 경과되지 않는 건만 재전송이 가능합니다.
         * - 팩스 재전송 요청시 포인트가 차감됩니다. (전송실패시 환불처리)
         * - 변환실패 사유로 전송실패한 팩스 접수건은 재전송이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/send#ResendFAXRNMulti
         */

        // 재전송 팩스의 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        // 발신번호, 공백처리시 기존전송정보로 재전송
        String sendNum = "07043042991";

        // 발신자명, 공백처리시 기존전송정보로 재전송
        String sendName = "발신자명";

        // 팩스수신정보를 기존전송정보와 동일하게 재전송하는 경우, receivers 변수 null 처리
        Receiver[] receivers = null;

        // 팩스수신정보를 기존전송정보와 다르게 재전송하는 경우, 아래의 코드 적용 (최대 1000건)
        // Receiver[] receivers = new Receiver[2];

        // Receiver receiver1 = new Receiver();
        // receiver1.setReceiveName("수신자1");      // 수신자명
        // receiver1.setReceiveNum("010111222");    // 수신팩스번호
        // receiver1.setInterOPRefKey("20221006-reFAXRN01");  // 파트너 지정키
        // receivers[0] = receiver1;

        // Receiver receiver2 = new Receiver();
        // receiver2.setReceiveName("수신자2");      // 수신자명
        // receiver2.setReceiveNum("010333444");    // 수신팩스번호
        // receiver2.setInterOPRefKey("20221006-reFAXRN02");  // 파트너 지정키
        // receivers[1] = receiver2;

        // 예약전송일시, null인 경우 즉시전송
        Date reserveDT = null;

        // 팩스제목
        String title = "팩스 재전송(동보) 제목";

        // 원본 팩스 전송시 파트너가 할당한 전송요청번호(requestNum)
        String orgRequestNum = "20221006-request";

        try {

            String receiptNum = faxService.resendFAXRN(CorpNum, requestNum, sendNum,
                    sendName, receivers, reserveDT, UserID, title, orgRequestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "cancelReserve", method = RequestMethod.GET)
    public String cancelReserve(Model m) {
        /**
         * 팝빌에서 반환받은 접수번호를 통해 예약접수된 팩스 전송을 취소합니다. (예약시간 10분 전까지 가능)
         * - https://developers.popbill.com/reference/fax/java/api/send#CancelReserve
         */

        // 예약팩스 전송요청시 팝빌로부터 반환 받은 접수번호
        String receiptNum = "022021803102600001";

        try {
            Response response = faxService.cancelReserve(CorpNum, receiptNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "cancelReserveRN", method = RequestMethod.GET)
    public String cancelReserveRN(Model m) {
        /**
         * 파트너가 할당한 전송요청 번호를 통해 예약접수된 팩스 전송을 취소합니다. (예약시간 10분 전까지 가능)
         * - https://developers.popbill.com/reference/fax/java/api/send#CancelReserveRN
         */

        // 예약팩스 전송요청시 파트너가 할당한 전송요청번호
        String requestNum = "";

        try {
            Response response = faxService.cancelReserveRN(CorpNum, requestNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getFaxResult", method = RequestMethod.GET)
    public String getFaxResult(Model m) {
        /**
         * 팝빌에서 반환 받은 접수번호를 통해 팩스 전송상태 및 결과를 확인합니다.
         * - https://developers.popbill.com/reference/fax/java/api/info#GetFaxResult
         */

        // 팩스 전송요청시 발급받은 접수번호
        String receiptNum = "022100616261900001";

        try {
            FaxResult[] faxResults = faxService.getFaxResult(CorpNum, receiptNum);

            m.addAttribute("FaxResults", faxResults);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Fax/FaxResult";
    }

    @RequestMapping(value = "getFaxResultRN", method = RequestMethod.GET)
    public String getFaxResultRN(Model m) {
        /**
         * 파트너가 할당한 전송요청 번호를 통해 팩스 전송상태 및 결과를 확인합니다.
         * - https://developers.popbill.com/reference/fax/java/api/info#GetFaxResultRN
         */

        // 팩스 전송요청시 파트너가 할당한 전송요청번호
        String requestNum = "";

        try {
            FaxResult[] faxResults = faxService.getFaxResultRN(CorpNum, requestNum);

            m.addAttribute("FaxResults", faxResults);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Fax/FaxResult";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /**
         * 검색조건에 해당하는 팩스 전송내역 목록을 조회합니다. (조회기간 단위 : 최대 2개월)
         * - 팩스 접수일시로부터 2개월 이내 접수건만 조회할 수 있습니다.
         * - https://developers.popbill.com/reference/fax/java/api/info#Search
         */

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20230102";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20230131";

        // 전송상태 배열 ("1" , "2" , "3" , "4" 중 선택, 다중 선택 가능)
        // └ 1 = 대기 , 2 = 성공 , 3 = 실패 , 4 = 취소
        // - 미입력 시 전체조회
        String[] State = {"1", "2", "3", "4"};

        // 예약여부 (false , true 중 택 1)
        // └ false = 전체조회, true = 예약전송건 조회
        // - 미입력시 기본값 false 처리
        Boolean ReserveYN = false;

        // 개인조회 여부 (false , true 중 택 1)
        // false = 접수한 팩스 전체 조회 (관리자권한)
        // true = 해당 담당자 계정으로 접수한 팩스만 조회 (개인권한)
        // 미입력시 기본값 false 처리
        Boolean SenderOnly = false;

        // 페이지 번호
        int Page = 1;

        // 페이지당 표시할 목록 개수 (최대 1000)
        int PerPage = 100;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        // 조회하고자 하는 발신자명 또는 수신자명
        // - 미입력시 전체조회
        String QString = "";

        try {

            FAXSearchResult response = faxService.search(CorpNum, SDate, EDate,
                    State, ReserveYN, SenderOnly, Page, PerPage, Order, QString);

            m.addAttribute("SearchResult", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Fax/SearchResult";
    }

    @RequestMapping(value = "getSentListURL", method = RequestMethod.GET)
    public String getSentListURL(Model m) {
        /**
         * 팩스 전송내역 확인 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/info#GetSentListURL
         */
        try {

            String url = faxService.getSentListURL(CorpNum, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getPreviewURL", method = RequestMethod.GET)
    public String getPrevewURL(Model m) {
        /**
         * 팩스 미리보기 팝업 URL을 반환하며, 팩스전송을 위한 TIF 포맷 변환 완료 후 호출 할 수 있습니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://developers.popbill.com/reference/fax/java/api/info#GetPreviewURL
         */
        try {

            // 팩스 접수번호
            String receiptNum = "022021803102600001";

            String url = faxService.getPreviewURL(CorpNum, receiptNum, UserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getUnitCost", method = RequestMethod.GET)
    public String getUnitCost(Model m) {
        /**
         * 팩스 전송시 과금되는 포인트 단가를 확인합니다.
         * - https://developers.popbill.com/reference/fax/java/api/point#GetUnitCost
         */

        try {

             // 수신번호 유형, 일반 / 지능 중 택 1
            String receiveNumType = "지능";

            float unitCost = faxService.getUnitCost(CorpNum, receiveNumType);

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
         * 팝빌 팩스 API 서비스 과금정보를 확인합니다.
         * - https://developers.popbill.com/reference/fax/java/api/point#GetChargeInfo
         */

        try {

            // 수신번호 유형, 일반 / 지능 중 택 1
            String receiveNumType = "일반";

            ChargeInfo chrgInfo = faxService.getChargeInfo(CorpNum, receiveNumType);

            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }
}
