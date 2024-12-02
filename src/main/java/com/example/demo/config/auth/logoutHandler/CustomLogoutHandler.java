package com.example.demo.config.auth.logoutHandler;

import com.example.demo.config.PrincipalDetails;
import com.example.demo.config.auth.jwt.JwtProperties;
import com.example.demo.config.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

public class CustomLogoutHandler implements LogoutHandler {

    private String NAVER_CLIENT_ID="z6yn5IQQPYzpAAIoJvS5";
    private String NAVER_CLIENT_SECRET="JyMQDaCyiN";

    private RestTemplate restTemplate;

    public CustomLogoutHandler() {
        restTemplate = new RestTemplate();
    }

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        System.out.println("CustomLogoutHandler's logout()...persistentTokenRepository : " + persistentTokenRepository);


        //OAUTH2 Client Logout Request


        //----------------------------
        //JWT
        //----------------------------
        //Cookie -> JWT Token 가져오기
        String token = Arrays.stream(request.getCookies())
                .filter(cookie->cookie.getName().equals(JwtProperties.COOKIE_NAME)).findFirst()
                .map(cookie -> cookie.getValue())
                .orElse(null);
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
////        persistentTokenRepository.removeUserTokens(authentication.getName());
//        if (request.getCookies() != null) {
//            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
//                if ("JSESSIONID".equals(cookie.getName())) {
//                    cookie.setValue(null);
//                    cookie.setPath("/");
//                    cookie.setMaxAge(0);
//                    response.addCookie(cookie);
//                }
//            }
//        }
//        request.getSession().invalidate();

        HttpSession session = request.getSession(false);
        if(session!=null)
            session.invalidate();
        PrincipalDetails principalDetails= (PrincipalDetails)authentication.getPrincipal();
        String provider =  principalDetails.getOauserDto().getProvider(); //kakao,google,naver

        //---------------------------------
        //REMEMBERME_DELETE
        //---------------------------------








        if(provider!=null && provider.startsWith("kakao")){

            String accessToken = principalDetails.getAccessToken();


            //1)URL
            String url="https://kapi.kakao.com/v1/user/logout";
            //2)HEADER
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type","application/x-www-form-urlencoded");
            headers.add("Authorization","Bearer "+accessToken);
            //3)PARAMS
            MultiValueMap<String,String> params = new LinkedMultiValueMap<>();

            //4)ENTITY
            HttpEntity< MultiValueMap<String,String> > entity = new HttpEntity(params,headers);

            //5)REQUEST
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> resp =
                    rt.exchange(url, HttpMethod.POST,entity,String.class);

        }else if(provider!=null && provider.startsWith("naver")){
            String accessToken = principalDetails.getAccessToken();
            //URL
            String url="https://nid.naver.com/oauth2.0/token?grant_type=delete&client_id="+NAVER_CLIENT_ID+"&client_secret="+NAVER_CLIENT_SECRET+"&access_token="+accessToken;
            //HEADER
            HttpHeaders headers = new HttpHeaders();
            //PARAM
            MultiValueMap<String,String> params = new LinkedMultiValueMap<>();

            //ENTITY
            HttpEntity< MultiValueMap<String,String> > entity = new HttpEntity(params,headers);

            //REQUEST
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> resp = rt.exchange(url, HttpMethod.GET,null,String.class);

            //RESPONSE
            System.out.println(resp.getBody());
        }else if(provider!=null && provider.startsWith("google")){
            String accessToken = principalDetails.getAccessToken();
            //URL
            String url="https://accounts.google.com/o/oauth2/revoke?token="+accessToken;
            //HEADER
            HttpHeaders headers = new HttpHeaders();
            //PARAM
            MultiValueMap<String,String> params = new LinkedMultiValueMap<>();

            //ENTITY
            HttpEntity< MultiValueMap<String,String> > entity = new HttpEntity(params,headers);

            //REQUEST
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> resp = rt.exchange(url, HttpMethod.GET,null,String.class);

            //RESPONSE
            System.out.println(resp.getBody());


        }



        //6)RESPONSE



        //Naver Logout

//        //1)URL
//        String url="";
//        //2)HEADER
//        HttpHeaders headers = new HttpHeaders();
//        //3)PARAMS
//        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
//
//        //4)ENTITY
//        HttpEntity< MultiValueMap<String,String> > entity = new HttpEntity(params,headers);
//
//        //5)REQUEST
//        RestTemplate rt = new RestTemplate();
//        ResponseEntity<String> resp =
//                rt.exchange(url, HttpMethod.GET,entity,String.class);

        //Google Logout


    }
}
