package com.library.common.service.impl;

import com.library.common.service.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service("commnServiceImpl")
public class CommonServiceImpl implements CommonService {

    @Override
    public void removeSessionMessage() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute("succMsg");
        session.removeAttribute("errorMsg");
    }
}
