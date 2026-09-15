package com.flittly.exception;

import com.flittly.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理。
 * <p>
 * 有人会担心「异常被这里吃掉，Seata 就不会回滚了吧」——不会。
 * 这个 Handler 在 Controller 之后才生效，而 @GlobalTransactional 的切面在
 * BusinessService.purchase() 退出时就已经看到异常并完成了回滚，之后才继续往外抛到这里。
 * <p>
 * 反过来，如果你在 BusinessService 内部自己 try-catch 把异常吞掉，那 Seata 就真的会当成成功提交。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R handle(Exception e) {
        log.error("全局事务已回滚, 原因: {}", e.getMessage(), e);
        return R.error(e.getMessage());
    }
}
