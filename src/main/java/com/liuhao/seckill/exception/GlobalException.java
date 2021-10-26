package com.liuhao.seckill.exception;

import com.liuhao.seckill.vo.RespBean;
import com.liuhao.seckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局异常
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalException extends RuntimeException{
    private RespBeanEnum respBeanEnum;
}
