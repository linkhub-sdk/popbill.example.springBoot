package com.popbill.example.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.popbill.api.ChargeInfo;
import com.popbill.api.KakaoService;
import com.popbill.api.PopbillException;
import com.popbill.api.Response;
import com.popbill.api.kakao.ATSTemplate;
import com.popbill.api.kakao.KakaoButton;
import com.popbill.api.kakao.KakaoReceiver;
import com.popbill.api.kakao.KakaoSearchResult;
import com.popbill.api.kakao.KakaoSentInfo;
import com.popbill.api.kakao.KakaoType;
import com.popbill.api.kakao.PlusFriendID;
import com.popbill.api.kakao.SenderNumber;

@Controller
@RequestMapping("KakaoService")
public class KakaoServiceController {

    @Autowired
    private KakaoService kakaoService;

    // 팝빌회원 사업자번호
    private String testCorpNum = "1234567890";

    // 팝빌회원 아이디
    private String testUserID = "testkorea";

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String home(Locale locale, Model model) {
        return "Kakao/index";
    }

    @RequestMapping(value = "getPlusFriendMgtURL", method = RequestMethod.GET)
    public String getPlusFriendMgtURL(Model m) {
        /*
         * 카카오톡 채널을 등록하고 내역을 확인하는 카카오톡 채널 관리 페이지 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/kakao/java/api#GetPlusFriendMgtURL
         */
        try {

            String url = kakaoService.getPlusFriendMgtURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "listPlusFriendID", method = RequestMethod.GET)
    public String listPlusFriendID(Model m) {
        /*
         * 팝빌에 등록한 연동회원의 카카오톡 채널 목록을 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#ListPlusFriendID
         */

        try {
            PlusFriendID[] response = kakaoService.listPlusFriendID(testCorpNum);

            m.addAttribute("listInfo", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Kakao/listPlusFriend";
    }

    @RequestMapping(value = "getSenderNumberMgtURL", method = RequestMethod.GET)
    public String getSenderNumberMgtURL(Model m) {
        /*
         * 발신번호를 등록하고 내역을 확인하는 카카오톡 발신번호 관리 페이지 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/kakao/java/api#GetSenderNumberMgtURL
         */
        try {

            String url = kakaoService.getSenderNumberMgtURL(testCorpNum, testUserID);

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
         * 팝빌에 등록한 연동회원의 카카오톡 발신번호 목록을 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#GetSenderNumberList
         */

        try {
            SenderNumber[] senderNumberList = kakaoService.getSenderNumberList(testCorpNum);
            m.addAttribute("SenderNumberList", senderNumberList);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Kakao/SenderNumber";
    }

    @RequestMapping(value = "getATSTemplateMgtURL", method = RequestMethod.GET)
    public String getATSTemplateMgtURL(Model m) {
        /*
         * 알림톡 템플릿을 신청하고 승인심사 결과를 확인하며 등록 내역을 확인하는 알림톡 템플릿 관리 페이지 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/kakao/java/api#GetATSTemplateMgtURL
         */
        try {

            String url = kakaoService.getATSTemplateMgtURL(testCorpNum, testUserID);

            m.addAttribute("Result", url);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "getATSTemplate", method = RequestMethod.GET)
    public String getATSTemplate(Model m) {
        /*
         * 승인된 알림톡 템플릿 정보를 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#GetATSTemplate
         */

        // 확인할 알림톡 템플릿 코드
        String templateCode = "021010000078";

        try {

            ATSTemplate response = kakaoService.getATSTemplate(testCorpNum, templateCode, testUserID);

            m.addAttribute("Template", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Kakao/getATSTemplate";
    }

    @RequestMapping(value = "listATSTemplate", method = RequestMethod.GET)
    public String listATSTemplate(Model m) {
        /*
         * 승인된 알림톡 템플릿 목록을 확인합니다.
         * - 반환항목중 템플릿코드(templateCode)는 알림톡 전송시 사용됩니다.
         * - https://docs.popbill.com/kakao/java/api#ListATSTemplate
         */

        try {
            ATSTemplate[] response = kakaoService.listATSTemplate(testCorpNum);

            m.addAttribute("listTemplate", response);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Kakao/listATSTemplate";
    }

    @RequestMapping(value = "sendATS_one", method = RequestMethod.GET)
    public String sendATS_one(Model m) {
        /*
         * 승인된 템플릿의 내용을 작성하여 1건의 알림톡 전송을 팝빌에 접수합니다.
         * - 사전에 승인된 템플릿의 내용과 알림톡 전송내용(content)이 다를 경우 전송실패 처리됩니다.
         * - https://docs.popbill.com/kakao/java/api#SendATS_one
         */

        // 알림톡 템플릿코드
        // 승인된 알림톡 템플릿 코드는 ListATStemplate API, GetATSTemplateMgtURL API, 또는 팝빌사이트에서 확인 가능합니다.
        String templateCode = "021020000163";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 알림톡 내용 (최대 1000자)
        String content = "[ 팝빌 ]\n";
        content += "신청하신 #{템플릿코드}에 대한 심사가 완료되어 승인 처리되었습니다.\n";
        content += "해당 템플릿으로 전송 가능합니다.\n\n";
        content += "문의사항 있으시면 파트너센터로 편하게 연락주시기 바랍니다.\n\n";
        content += "팝빌 파트너센터 : 1600-8536\n";
        content += "support@linkhub.co.kr";

        // 대체문자 내용 (최대 2000byte)
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "C";

        // 수신번호
        String receiverNum = "010111222";

        // 수신자명
        String receiverName = "수신자명";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        // 알림톡 버튼정보를 템플릿 신청시 기재한 버튼정보와 동일하게 전송하는 경우 null 처리.
        KakaoButton[] btns = null;

        // 알림톡 버튼 URL에 #{템플릿변수}를 기재한경우 템플릿변수 영역을 변경하여 버튼정보 구성
//        KakaoButton[] btns = new KakaoButton[1];
//
//        KakaoButton button = new KakaoButton();
//        button.setN("버튼명"); // 버튼명
//        button.setT("WL"); // 버튼타입
//        button.setU1("https://www.popbill.com"); // 버튼링크1
//        button.setU2("http://test.popbill.com"); // 버튼링크2
//        btns[0] = button;

        try {

            String receiptNum = kakaoService.sendATS(testCorpNum, templateCode, senderNum, content, altContent, altSendType, receiverNum,
                    receiverName, sndDT, testUserID, requestNum, btns);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendATS_multi", method = RequestMethod.GET)
    public String sendATS_multi(Model m) {
        /*
         * 승인된 템플릿의 내용을 작성하여 다수건의 알림톡 전송을 팝빌에 접수하며, 수신자 별로 개별 내용을 전송합니다. (최대 1,000건)
         * - 사전에 승인된 템플릿의 내용과 알림톡 전송내용(content)이 다를 경우 전송실패 처리됩니다.
         * - https://docs.popbill.com/kakao/java/api#SendATS_multi
         */

        // 알림톡 템플릿코드
        // 승인된 알림톡 템플릿 코드는 ListATStemplate API, GetATSTemplateMgtURL API, 또는 팝빌사이트에서 확인 가능합니다.
        String templateCode = "021020000163";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "";

        // 알림톡 내용 (최대 1000자)
        String content = "[ 팝빌 ]\n";
        content += "신청하신 #{템플릿코드}에 대한 심사가 완료되어 승인 처리되었습니다.\n";
        content += "해당 템플릿으로 전송 가능합니다.\n\n";
        content += "문의사항 있으시면 파트너센터로 편하게 연락주시기 바랍니다.\n\n";
        content += "팝빌 파트너센터 : 1600-8536\n";
        content += "support@linkhub.co.kr";

        // 카카오톡 수신정보 배열, 최대 1000건
        KakaoReceiver[] receivers = new KakaoReceiver[10];
        for (int i = 0; i < 10; i++) {
            KakaoReceiver message = new KakaoReceiver();
            message.setReceiverNum("010111222"); // 수신번호
            message.setReceiverName("수신자명" + i); // 수신자명
            message.setMessage(content); // 알림톡 템플릿 내용, 최대 1000자
            message.setAltMessage("대체문자 개별내용입니다." + i); // 대체문자 내용

            // 수신자별 개별 버튼정보
//            KakaoButton button = new KakaoButton();
//            button.setN("타입1 버튼명"+i); // 버튼명
//            button.setT("WL"); // 버튼타입
//            button.setU1("http://"+i+"popbill.com"); // 버튼링크1
//            button.setU2("http://"+i+"test.popbill.com"); // 버튼링크2
//
//            KakaoButton button02 = new KakaoButton();
//            button02.setN("타입2 버튼명"+i); // 버튼명
//            button02.setT("WL"); // 버튼타입
//            button02.setU1("http://"+i+"popbill.com"); // 버튼링크1
//            button02.setU2("http://"+i+"test.popbill.com"); // 버튼링크2
//
//            // 수신자별로 각기다른 버튼정보 추가.
//            message.setBtns(new ArrayList<KakaoButton>());
//            message.getBtns().add(button);
//            message.getBtns().add(button02);
            receivers[i] = message;
        }

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        // 알림톡 버튼정보를 템플릿 신청시 기재한 버튼정보와 동일하게 전송하는 경우 null 처리.
        KakaoButton[] btns = null;

        // 알림톡 버튼 URL에 #{템플릿변수}를 기재한경우 템플릿변수 영역을 변경하여 버튼정보 구성
//        KakaoButton[] btns = new KakaoButton[1];
//
//        KakaoButton button = new KakaoButton();
//        button.setN("버튼명"); // 버튼명
//        button.setT("WL"); // 버튼타입
//        button.setU1("https://www.popbill.com"); // 버튼링크1
//        button.setU2("http://test.popbill.com"); // 버튼링크2
//        btns[0] = button;

        try {

            String receiptNum = kakaoService.sendATS(testCorpNum, templateCode, senderNum, altSendType, receivers, sndDT, testUserID, requestNum,
                    btns);
            m.addAttribute("Result", receiptNum);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "result";
    }

    @RequestMapping(value = "sendATS_same", method = RequestMethod.GET)
    public String sendATS_same(Model m) {
        /*
         * 승인된 템플릿 내용을 작성하여 다수건의 알림톡 전송을 팝빌에 접수하며, 모든 수신자에게 동일 내용을 전송합니다. (최대 1,000건)
         * - 사전에 승인된 템플릿의 내용과 알림톡 전송내용(content)이 다를 경우 전송실패 처리됩니다.
         * - https://docs.popbill.com/kakao/java/api#SendATS_same
         */

        // 알림톡 템플릿코드
        // 승인된 알림톡 템플릿 코드는 ListATStemplate API, GetATSTemplateMgtURL API, 또는 팝빌사이트에서 확인 가능합니다.
        String templateCode = "021020000163";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 알림톡 내용 (최대 1000자)
        String content = "[ 팝빌 ]\n";
        content += "신청하신 #{템플릿코드}에 대한 심사가 완료되어 승인 처리되었습니다.\n";
        content += "해당 템플릿으로 전송 가능합니다.\n\n";
        content += "문의사항 있으시면 파트너센터로 편하게 연락주시기 바랍니다.\n\n";
        content += "팝빌 파트너센터 : 1600-8536\n";
        content += "support@linkhub.co.kr";

        // 대체문자 내용 (최대 2000byte)
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "C";

        // 카카오톡 수신정보 배열, 최대 1000건
        KakaoReceiver[] receivers = new KakaoReceiver[10];
        for (int i = 0; i < 10; i++) {
            KakaoReceiver message = new KakaoReceiver();
            message.setReceiverNum("010111222"); // 수신번호
            message.setReceiverName("수신자명" + i); // 수신자명
            receivers[i] = message;
        }

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        // 알림톡 버튼정보를 템플릿 신청시 기재한 버튼정보와 동일하게 전송하는 경우 null 처리.
        KakaoButton[] btns = null;

        // 알림톡 버튼 URL에 #{템플릿변수}를 기재한경우 템플릿변수 영역을 변경하여 버튼정보 구성
//        KakaoButton[] btns = new KakaoButton[1];
//
//        KakaoButton button = new KakaoButton();
//        button.setN("버튼명"); // 버튼명
//        button.setT("WL"); // 버튼타입
//        button.setU1("https://www.popbill.com"); // 버튼링크1
//        button.setU2("http://test.popbill.com"); // 버튼링크2
//        btns[0] = button;

        try {

            String receiptNum = kakaoService.sendATS(testCorpNum, templateCode, senderNum, content, altContent, altSendType, receivers, sndDT,
                    testUserID, requestNum, btns);
            m.addAttribute("Result", receiptNum);
        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "result";
    }

    @RequestMapping(value = "sendFTS_one", method = RequestMethod.GET)
    public String sendFTS_one(Model m) {
        /*
         * 텍스트로 구성된 1건의 친구톡 전송을 팝빌에 접수합니다.
         * - 친구톡의 경우 야간 전송은 제한됩니다. (20:00 ~ 익일 08:00)
         * - https://docs.popbill.com/kakao/java/api#SendFTS_one
         */

        // 팝빌에 등록된 카카오톡 채널 아이디
        String plusFriendID = "@팝빌";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 친구톡 내용 (최대 1000자)
        String content = "친구톡 메시지 내용";

        // 대체문자 내용
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 친구톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "A";

        // 수신번호
        String receiverNum = "010111222";

        // 수신자명
        String receiverName = "수신자명";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 광고전송여부
        Boolean adsYN = false;

        // 친구톡 버튼 배열, 최대 5개
        KakaoButton[] btns = new KakaoButton[2];

        KakaoButton button = new KakaoButton();
        button.setN("버튼명");                    // 버튼명
        button.setT("WL");                       // 버튼타입
        button.setU1("http://www.popbill.com");  // 버튼링크1
        button.setU2("http://test.popbill.com"); // 버튼링크2
        btns[0] = button;

        KakaoButton button2 = new KakaoButton();
        button2.setN("버튼명2");
        button2.setT("WL");
        button2.setU1("http://www.popbill.com");
        button2.setU2("http://test.popbill.com");
        btns[1] = button2;

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = kakaoService.sendFTS(testCorpNum, plusFriendID, senderNum, content, altContent, altSendType, btns, receiverNum,
                    receiverName, sndDT, adsYN, testUserID, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFTS_multi", method = RequestMethod.GET)
    public String sendFTS_multi(Model m) {
        /*
         * 텍스트로 구성된 다수건의 친구톡 전송을 팝빌에 접수하며, 수신자 별로 개별 내용을 전송합니다. (최대 1,000건)
         * - 친구톡의 경우 야간 전송은 제한됩니다. (20:00 ~ 익일 08:00)
         * - https://docs.popbill.com/kakao/java/api#SendFTS_multi
         */

        // 팝빌에 등록된 카카오톡 채널 아이디
        String plusFriendID = "@팝빌";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "A";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 광고전송여부
        Boolean adsYN = false;

        // 카카오톡 수신정보 배열, 최대 1000건
        KakaoReceiver[] receivers = new KakaoReceiver[10];
        for (int i = 0; i < 10; i++) {
            KakaoReceiver message = new KakaoReceiver();
            message.setReceiverNum("010111222");                 // 수신번호
            message.setReceiverName("수신자명" + i);             // 수신자명
            message.setMessage("친구톡 개별내용" + i);           // 친구톡 내용, 최대 1000자
            message.setAltMessage("대체문자 개별내용" + i);      // 대체문자 내용
            message.setInterOPRefKey("");      // 파트너 지정키, 대량 접수시 수신자 구별용 메모.

            KakaoButton button = new KakaoButton();
            button.setN("타입1 버튼명" + i);                     // 버튼명
            button.setT("WL");                                  // 버튼타입
            button.setU1("http://" + i + "popbill.com");        // 버튼링크1
            button.setU2("http://" + i + "test.popbill.com");   // 버튼링크2

            KakaoButton button02 = new KakaoButton();
            button02.setN("타입2 버튼명" + i);                   // 버튼명
            button02.setT("WL");                                // 버튼타입
            button02.setU1("http://" + i + "popbill.com");      // 버튼링크1
            button02.setU2("http://" + i + "test.popbill.com"); // 버튼링크2

            // 수신자별로 각기다른 버튼정보 추가.
            message.setBtns(new ArrayList<KakaoButton>());
            message.getBtns().add(button);
            message.getBtns().add(button02);

            receivers[i] = message;
        }

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = kakaoService.sendFTS(testCorpNum, plusFriendID, senderNum, altSendType, receivers, null, sndDT, adsYN, testUserID,
                    requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFTS_same", method = RequestMethod.GET)
    public String sendFTS_same(Model m) {
        /*
         * 텍스트로 구성된 다수건의 친구톡 전송을 팝빌에 접수하며, 모든 수신자에게 동일 내용을 전송합니다. (최대 1,000건)
         * - 친구톡의 경우 야간 전송은 제한됩니다. (20:00 ~ 익일 08:00)
         * - https://docs.popbill.com/kakao/java/api#SendFTS_same
         */

        // 팝빌에 등록된 카카오톡 채널 아이디
        String plusFriendID = "@팝빌";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 친구톡 내용 (최대 1000자)
        String content = "친구톡 메시지 내용";

        // 대체문자 내용
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "A";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 광고전송여부
        Boolean adsYN = false;

        // 카카오톡 수신정보 배열, 최대 1000건
        KakaoReceiver[] receivers = new KakaoReceiver[100];
        for (int i = 0; i < 100; i++) {
            KakaoReceiver message = new KakaoReceiver();
            message.setReceiverNum("010111222");
            message.setReceiverName("수신자명" + i);
            receivers[i] = message;
        }

        // 친구톡 버튼 배열, 최대 5개
        KakaoButton[] btns = new KakaoButton[2];

        KakaoButton button = new KakaoButton();
        button.setN("버튼명");                    // 버튼명
        button.setT("WL");                       // 버튼타입
        button.setU1("http://www.popbill.com");  // 버튼링크1
        button.setU2("http://test.popbill.com"); // 버튼링크2
        btns[0] = button;

        KakaoButton button2 = new KakaoButton();
        button2.setN("버튼명2");
        button2.setT("WL");
        button2.setU1("http://www.popbill.com");
        button2.setU2("http://test.popbill.com");
        btns[1] = button2;

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = kakaoService.sendFTS(testCorpNum, plusFriendID, senderNum, content, altContent, altSendType, receivers, btns, sndDT,
                    adsYN, testUserID, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFMS_one", method = RequestMethod.GET)
    public String sendFMS_one(Model m) {
        /*
         * 이미지가 첨부된 1건의 친구톡 전송을 팝빌에 접수합니다.
         * - 친구톡의 경우 야간 전송은 제한됩니다. (20:00 ~ 익일 08:00)
         * - 이미지 파일 규격: 전송 포맷 – JPG 파일 (.jpg, .jpeg), 용량 – 최대 500 Kbyte, 크기 – 가로 500px 이상, 가로 기준으로 세로 0.5~1.3배 비율 가능
         * - https://docs.popbill.com/kakao/java/api#SendFMS_one
         */

        // 팝빌에 등록된 카카오톡 채널 아이디
        String plusFriendID = "@팝빌";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 친구톡 내용 (최대 400자)
        String content = "친구톡 메시지 내용";

        // 대체문자 내용 (최대 2000byte)
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 알림톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "A";

        // 수신번호
        String receiverNum = "010111222";

        // 수신자명
        String receiverName = "수신자명";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 광고전송여부
        Boolean adsYN = false;

        // 친구톡 버튼 배열, 최대 5개
        KakaoButton[] btns = new KakaoButton[2];

        KakaoButton button = new KakaoButton();
        button.setN("버튼명");                    // 버튼명
        button.setT("WL");                       // 버튼타입
        button.setU1("http://www.popbill.com");  // 버튼링크1
        button.setU2("http://test.popbill.com"); // 버튼링크2
        btns[0] = button;

        KakaoButton button2 = new KakaoButton();
        button2.setN("버튼명2");
        button2.setT("WL");
        button2.setU1("http://www.popbill.com");
        button2.setU2("http://test.popbill.com");
        btns[1] = button2;

        // 첨부이미지 파일 경로
        // 이미지 파일 규격: 전송 포맷 – JPG 파일 (.jpg, .jpeg), 용량 – 최대 500 Kbyte, 크기 – 가로 500px 이상, 가로 기준으로 세로 0.5~1.3배 비율 가능
        File file = new File("/Users/John/Desktop/tmp/test03.jpg");

        // 이미지 링크 URL
        // └ 수신자가 친구톡 상단 이미지 클릭시 호출되는 URL
        // └ 미입력시 첨부된 이미지를 링크 기능 없이 표시
        String imageURL = "http://test.popbill.com";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = kakaoService.sendFMS(testCorpNum, plusFriendID, senderNum, content, altContent, altSendType, btns, receiverNum,
                    receiverName, sndDT, adsYN, file, imageURL, testUserID, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFMS_multi", method = RequestMethod.GET)
    public String sendFMS_multi(Model m) {
        /*
         * 이미지가 첨부된 다수건의 친구톡 전송을 팝빌에 접수하며, 수신자 별로 개별 내용을 전송합니다. (최대 1,000건)
         * - 친구톡의 경우 야간 전송은 제한됩니다. (20:00 ~ 익일 08:00)
         * - 이미지 파일 규격: 전송 포맷 – JPG 파일 (.jpg, .jpeg), 용량 – 최대 500 Kbyte, 크기 – 가로 500px 이상, 가로 기준으로 세로 0.5~1.3배 비율 가능
         * - https://docs.popbill.com/kakao/java/api#SendFMS_multi
         */

        // 팝빌에 등록된 카카오톡 채널 아이디
        String plusFriendID = "@팝빌";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 친구톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "A";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 광고전송여부
        Boolean adsYN = false;

        // 카카오톡 수신정보 배열, 최대 1000건
        KakaoReceiver[] receivers = new KakaoReceiver[100];
        for (int i = 0; i < 100; i++) {
            KakaoReceiver message = new KakaoReceiver();
            message.setReceiverNum("010111222");            // 수신번호
            message.setReceiverName("수신자명" + i);        // 수신자명
            message.setMessage("친구톡 개별내용" + i);      // 친구톡 내용, 최대 400자
            message.setAltMessage("대체문자 개별내용" + i); // 대체문자 내용
            receivers[i] = message;

            // 수신자별 개별 버튼 정보
//            KakaoButton button = new KakaoButton();
//            button.setN("타입1 버튼명"+i); // 버튼명
//            button.setT("WL"); // 버튼타입
//            button.setU1("http://"+i+"popbill.com"); // 버튼링크1
//            button.setU2("http://"+i+"test.popbill.com"); // 버튼링크2
//
//            KakaoButton button02 = new KakaoButton();
//            button02.setN("타입2 버튼명"+i); // 버튼명
//            button02.setT("WL"); // 버튼타입
//            button02.setU1("http://"+i+"popbill.com"); // 버튼링크1
//            button02.setU2("http://"+i+"test.popbill.com"); // 버튼링크2
//
//            // 수신자별로 각기다른 버튼정보 추가.
//            message.setBtns(new ArrayList<KakaoButton>());
//            message.getBtns().add(button);
//            message.getBtns().add(button02);

        }

        // 수신자별 동일 버튼 정보
        // 친구톡 버튼 배열, 최대 5개
        KakaoButton[] btns = new KakaoButton[2];

        KakaoButton button = new KakaoButton();
        button.setN("버튼명");                    // 버튼명
        button.setT("WL");                       // 버튼타입
        button.setU1("http://www.popbill.com");  // 버튼링크1
        button.setU2("http://test.popbill.com"); // 버튼링크2
        btns[0] = button;

        KakaoButton button2 = new KakaoButton();
        button2.setN("버튼명2");
        button2.setT("WL");
        button2.setU1("http://www.popbill.com");
        button2.setU2("http://test.popbill.com");
        btns[1] = button2;

        // 첨부이미지 파일 경로
        // 이미지 파일 규격: 전송 포맷 – JPG 파일 (.jpg, .jpeg), 용량 – 최대 500 Kbyte, 크기 – 가로 500px 이상, 가로 기준으로 세로 0.5~1.3배 비율 가능
        File file = new File("/Users/John/Desktop/tmp/test03.jpg");

        // 이미지 링크 URL
        // └ 수신자가 친구톡 상단 이미지 클릭시 호출되는 URL
        // └ 미입력시 첨부된 이미지를 링크 기능 없이 표시
        String imageURL = "http://test.popbill.com";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = kakaoService.sendFMS(testCorpNum, plusFriendID, senderNum, altSendType, receivers, btns, sndDT, adsYN, file, imageURL,
                    testUserID, requestNum);

            m.addAttribute("Result", receiptNum);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "result";
    }

    @RequestMapping(value = "sendFMS_same", method = RequestMethod.GET)
    public String sendFMS_same(Model m) {
        /*
         * 이미지가 첨부된 다수건의 친구톡 전송을 팝빌에 접수하며, 모든 수신자에게 동일 내용을 전송합니다. (최대 1,000건)
         * - 친구톡의 경우 야간 전송은 제한됩니다. (20:00 ~ 익일 08:00)
         * - 이미지 파일 규격: 전송 포맷 – JPG 파일 (.jpg, .jpeg), 용량 – 최대 500 Kbyte, 크기 – 가로 500px 이상, 가로 기준으로 세로 0.5~1.3배 비율 가능
         * - https://docs.popbill.com/kakao/java/api#SendFMS_same
         */

        // 팝빌에 등록된 카카오톡 채널 아이디
        String plusFriendID = "@팝빌";

        // 발신번호 (팝빌에 등록된 발신번호만 이용가능)
        String senderNum = "07043042991";

        // 친구톡 내용 (최대 400자)
        String content = "친구톡 메시지 내용";

        // 대체문자 내용 (최대 2000byte)
        String altContent = "대체문자 내용";

        // 대체문자 유형 (null , "C" , "A" 중 택 1)
        // null = 미전송, C = 친구톡과 동일 내용 전송 , A = {altContent}에 입력한 내용 전송
        String altSendType = "A";

        // 예약전송일시, 형태(yyyyMMddHHmmss)
        String sndDT = "";

        // 광고전송여부
        Boolean adsYN = false;

        // 카카오톡 수신정보 배열, 최대 1000건
        KakaoReceiver[] receivers = new KakaoReceiver[100];
        for (int i = 0; i < 100; i++) {
            KakaoReceiver message = new KakaoReceiver();
            message.setReceiverNum("010111222");     // 수신번호
            message.setReceiverName("수신자명" + i); // 수신자명
            receivers[i] = message;
        }

        // 친구톡 버튼 배열, 최대 5개
        KakaoButton[] btns = new KakaoButton[2];

        KakaoButton button = new KakaoButton();
        button.setN("버튼명");                    // 버튼명
        button.setT("WL");                       // 버튼타입
        button.setU1("http://www.popbill.com");  // 버튼링크1
        button.setU2("http://test.popbill.com"); // 버튼링크2
        btns[0] = button;

        KakaoButton button2 = new KakaoButton();
        button2.setN("버튼명2");
        button2.setT("WL");
        button2.setU1("http://www.popbill.com");
        button2.setU2("http://test.popbill.com");
        btns[1] = button2;

        // 첨부이미지 파일 경로
        // 이미지 파일 규격: 전송 포맷 – JPG 파일 (.jpg, .jpeg), 용량 – 최대 500 Kbyte, 크기 – 가로 500px 이상, 가로 기준으로 세로 0.5~1.3배 비율 가능
        File file = new File("/Users/John/Desktop/tmp/test03.jpg");

        // 이미지 링크 URL
        // └ 수신자가 친구톡 상단 이미지 클릭시 호출되는 URL
        // └ 미입력시 첨부된 이미지를 링크 기능 없이 표시
        String imageURL = "http://test.popbill.com";

        // 전송요청번호
        // 파트너가 전송 건에 대해 관리번호를 구성하여 관리하는 경우 사용.
        // 1~36자리로 구성. 영문, 숫자, 하이픈(-), 언더바(_)를 조합하여 팝빌 회원별로 중복되지 않도록 할당.
        String requestNum = "";

        try {

            String receiptNum = kakaoService.sendFMS(testCorpNum, plusFriendID, senderNum, content, altContent, altSendType, receivers, btns, sndDT,
                    adsYN, file, imageURL, testUserID, requestNum);

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
         * 팝빌에서 반환받은 접수번호를 통해 예약접수된 카카오톡을 전송 취소합니다. (예약시간 10분 전까지 가능)
         * - https://docs.popbill.com/kakao/java/api#CancelReserve
         */

        // 카카오톡 예약전송 접수시 팝빌로부터 반환받은 접수번호
        String receiptNum = "022011014155400001";

        try {
            Response response = kakaoService.cancelReserve(testCorpNum, receiptNum);

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
         * 파트너가 할당한 전송요청 번호를 통해 예약접수된 카카오톡을 전송 취소합니다. (예약시간 10분 전까지 가능)
         * - https://docs.popbill.com/kakao/java/api#CancelReserveRN
         */

        // 카카오톡 예약전송 접수시 파트너가 할당한 전송요청 번호
        String requestNum = "";

        try {
            Response response = kakaoService.cancelReserveRN(testCorpNum, requestNum);

            m.addAttribute("Response", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "response";
    }

    @RequestMapping(value = "getMessages", method = RequestMethod.GET)
    public String getMessages(Model m) {
        /*
         * 팝빌에서 반환받은 접수번호를 통해 알림톡/친구톡 전송상태 및 결과를 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#GetMessages
         */

        // 카카오톡 전송 접수시 팝빌로부터 반환받은 접수번호
        String receiptNum = "022011014161600001";

        try {

            KakaoSentInfo sentInfos = kakaoService.getMessages(testCorpNum, receiptNum);

            m.addAttribute("sentInfos", sentInfos);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Kakao/getMessage";
    }

    @RequestMapping(value = "getMessagesRN", method = RequestMethod.GET)
    public String getMessagesRN(Model m) {
        /*
         * 파트너가 할당한 전송요청 번호를 통해 알림톡/친구톡 전송상태 및 결과를 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#GetMessagesRN
         */

        // 카카오톡 전송 접수시 파트너가 할당한 전송요청 번호
        String requestNum = "";

        try {

            KakaoSentInfo sentInfos = kakaoService.getMessagesRN(testCorpNum, requestNum);

            m.addAttribute("sentInfos", sentInfos);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "Kakao/getMessage";
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    public String search(Model m) {
        /*
         * 검색조건에 해당하는 카카오톡 전송내역을 조회합니다. (조회기간 단위 : 최대 2개월) -
         *  카카오톡 접수일시로부터 6개월 이내 접수건만 조회할 수 있습니다.
         *  - https://docs.popbill.com/kakao/java/api#Search
         */

        // 시작일자, 날짜형식(yyyyMMdd)
        String SDate = "20211119";

        // 종료일자, 날짜형식(yyyyMMdd)
        String EDate = "20220101";

        // 전송상태 배열 ("0" , "1" , "2" , "3" , "4" , "5" 중 선택, 다중 선택 가능)
        // └ 0 = 전송대기, 1 = 전송중 , 2 = 전송성공 , 3 = 대체문자 전송 , 4 = 전송실패 , 5 = 전송취소
        // - 미입력 시 전체조회
        String[] State = { "0", "1", "2", "3", "4" };

        // 검색대상 배열 ("ATS", "FTS", "FMS" 중 선택, 다중 선택 가능)
        // └ ATS = 알림톡, FTS = 친구톡(텍스트), FMS = 친구톡(이미지)
        String[] Item = { "ATS", "FTS", "FMS" };

        // 전송유형별 조회 (null , "0" , "1" 중 택 1)
        // └ null = 전체조회, 0 = 즉시전송건 조회, 1 = 예약전송건 조회
        String ReserveYN = "";

        // 사용자권한별 조회 (true / false 중 택 1)
        // - false = 접수한 카카오톡 전체 조회 (관리자권한)
        // - true = 해당 담당자 계정으로 접수한 카카오톡만 조회 (개인권한)
        // 미입력시 기본값 false 처리
        Boolean SenderYN = false;

        // 페이지 번호
        int Page = 1;

        // 페이지당 목록개수 (최대 1000건)
        int PerPage = 20;

        // 정렬방향 D-내림차순, A-오름차순
        String Order = "D";

        // 조회하고자 하는 수신자명
        // - 미입력시 전체조회
        String QString = "";

        try {

            KakaoSearchResult response = kakaoService.search(testCorpNum, SDate, EDate, State, Item, ReserveYN, SenderYN, Page, PerPage, Order,
                    testUserID, QString);

            m.addAttribute("SearchResult", response);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }
        return "Kakao/searchResult";
    }

    @RequestMapping(value = "getSentListURL", method = RequestMethod.GET)
    public String getSentListURL(Model m) {
        /*
         * 팝빌 사이트와 동일한 카카오톡 전송내역을 확인하는 페이지의 팝업 URL을 반환합니다.
         * - 반환되는 URL은 보안 정책상 30초 동안 유효하며, 시간을 초과한 후에는 해당 URL을 통한 페이지 접근이 불가합니다.
         * - https://docs.popbill.com/kakao/java/api#GetSentListURL
         */

        try {

            String url = kakaoService.getSentListURL(testCorpNum, testUserID);

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
         * 카카오톡 전송시 과금되는 포인트 단가를 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#GetUnitCost
         */

        // 카카오톡 전송유형, ATS-알림톡, FTS-친구톡 텍스트, FMS-친구톡 이미지
        KakaoType kakaoType = KakaoType.ATS;

        try {

            float unitCost = kakaoService.getUnitCost(testCorpNum, kakaoType);

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
         * 팝빌 카카오톡 API 서비스 과금정보를 확인합니다.
         * - https://docs.popbill.com/kakao/java/api#GetChargeInfo
         */

        // 카카오톡 전송유형, ATS-알림톡, FTS-친구톡 텍스트, FMS-친구톡 이미지
        KakaoType kakaoType = KakaoType.ATS;

        try {

            ChargeInfo chrgInfo = kakaoService.getChargeInfo(testCorpNum, kakaoType);
            m.addAttribute("ChargeInfo", chrgInfo);

        } catch (PopbillException e) {
            m.addAttribute("Exception", e);
            return "exception";
        }

        return "getChargeInfo";
    }

}
