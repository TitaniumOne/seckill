package com.liuhao.seckill.vo;

import com.liuhao.seckill.utils.ValidatorUtil;
import com.liuhao.seckill.validator.IsMobile;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 手机号码校验规则
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        constraintAnnotation.required();

        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(required) {
            return ValidatorUtil.isMobile(value);
        } else {
            if(StringUtils.isEmpty(value)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
