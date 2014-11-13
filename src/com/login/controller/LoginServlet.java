package com.login.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.company.model.CompanyService;
import com.company.model.CompanyVO;
import com.login.model.LoginService;
import com.member.model.MemService;
import com.member.model.MemVO;
import com.orders.controller.LoginOrdersOnLoad;
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		Map<String, String> errorMsg = new HashMap<String, String>();
		// Map<String, String> msgOK = new HashMap<String, String>();
		request.setAttribute("ErrorMsg", errorMsg); // 顯示錯誤訊息

		// request.setAttribute("MsgOK", msgOK); // 顯示成功訊息

		// 1. 讀取使用者資料
		String userAccount = request.getParameter("userAccount");
		String userPwd = request.getParameter("userPwd");
		String userIdentity = request.getParameter("optionsRadios");
//		String rm = request.getParameter("rememberMe");
		String requestURI = (String) session.getAttribute("requestURI");
		
		// 2. 檢查使用者輸入資料 (前端做, 這裡暫不做)
		// 如果 userId 欄位為空白，放一個錯誤訊息到 errorMsgMap 之內
//		if (userAccount == null || userAccount.trim().length() == 0) {
//			errorMsg.put("AccountEmptyError", "帳號欄必須輸入");
//		}
//		// 如果 password 欄位為空白，放一個錯誤訊息到 errorMsgMap 之內
//		if (userPwd == null || userPwd.trim().length() == 0) {
//			errorMsg.put("PasswordEmptyError", "密碼欄必須輸入");
//		}
		
		// RememberMe  (暫不做)
		Cookie cookieUser = null;
		Cookie cookiePassword = null;
		Cookie cookieRememberMe = null;
		
//		if (rm != null) {
//			cookieUser = new Cookie("user", userAccount);
//			cookieUser.setMaxAge(30*60*60);
//			cookieUser.setPath(request.getContextPath());
////			String encodePassword = Base64.encode(password.getBytes());
//			cookiePassword = new Cookie("password", userPwd);
//			cookiePassword.setMaxAge(30*60*60);
//			cookiePassword.setPath(request.getContextPath());
//			cookieRememberMe = new Cookie("rm", "true");
//			cookieRememberMe.setMaxAge(30*60*60);
//			cookieRememberMe.setPath(request.getContextPath());
//		} else {
//			cookieUser = new Cookie("user", userAccount);
//			cookieUser.setMaxAge(0);
//			cookieUser.setPath(request.getContextPath());
//			cookiePassword = new Cookie("password", userPwd);
////			String encodePassword = Base64.encode(password.getBytes());
////			cookiePassword = new Cookie("password", encodePassword);
//			cookiePassword.setMaxAge(0);
//			cookiePassword.setPath(request.getContextPath());
//			cookieRememberMe = new Cookie("rm", "false");
//			cookieRememberMe.setMaxAge(30*60*60);
//			cookieRememberMe.setPath(request.getContextPath());
//		}
//		response.addCookie(cookieUser);
//		response.addCookie(cookiePassword);
//		response.addCookie(cookieRememberMe);
		
		// 如果 errorMsgMap 不是空的，表示有錯誤，交棒給login.jsp
		if (!errorMsg.isEmpty()) {
			RequestDispatcher rd = request.getRequestDispatcher("/_01_login/login.jsp");
			rd.forward(request, response);
			return;
		}
		
		// 3. 進行 Business Logic 運算
		System.out.println("會員種類: " + userIdentity);
		
		Integer	userId = null; //要設初值才能傳進方法內
		MemVO memVO = null;
		CompanyVO comVO = null;
		try {
			LoginService loginService = new LoginService();
			userId =  loginService.loginCheckId(userAccount, userPwd, userIdentity);

			if ("Mem".equals(userIdentity)) {
				MemService memService = new MemService();
				System.out.println("mem會員ID = " + userId);
				memVO = memService.getOneMem(userId);
				session.setAttribute("LoginMemOK", memVO);
			}
			else if ("Com".equals(userIdentity)) {
				CompanyService comService = new CompanyService();
				comVO = comService.getOneCom(userId);
				session.setAttribute("LoginComOK", comVO);
				System.out.println("session裡面: " + comVO.getComName());
			}
			//阿阮新增-userIdentity存進session內比較好比對
			session.setAttribute("userIdentity", userIdentity);
			//阿阮新增-------------------------------
		} catch(Exception e) {
			e.getStackTrace();
			errorMsg.put("LoginError", "該帳號不存在或密碼錯誤");

		}

		// 4. 轉交
		if (errorMsg.isEmpty()) {
			if (memVO != null) {
				if (requestURI != null) {
					requestURI = (requestURI.length() == 0 ? request
							.getContextPath() : requestURI);
					response.sendRedirect(response.encodeRedirectURL(requestURI));
					return;
				} else {
					response.sendRedirect(response.encodeRedirectURL(request
							.getContextPath()));
					return;
				}
			}
			else if (comVO != null) {
				if (requestURI != null) {
					requestURI = (requestURI.length() == 0 ? request.getContextPath() + "/manage.jsp" : requestURI);
					response.sendRedirect(response.encodeRedirectURL(requestURI));
					return;
				} else {
					System.out.println("進入商家後端");
					//阿阮新增-----------
					LoginOrdersOnLoad OCOL = new LoginOrdersOnLoad();  
					session = OCOL.ComOnLoad(session,userId);
					//----------------
					response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/manage.jsp"));
					return;
				}
			}
		}
		else {
			// 如果errorMsgMap不是空的，表示有錯誤，交棒給login.jsp
						RequestDispatcher rd = request.getRequestDispatcher("/_01_login/login.jsp");
						rd.forward(request, response);
						return;
		}
	} // end doPost
}
