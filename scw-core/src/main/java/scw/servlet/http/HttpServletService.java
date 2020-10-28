package scw.servlet.http;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import scw.aop.annotation.AopEnable;

@AopEnable(false)
public interface HttpServletService {
	void service(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
