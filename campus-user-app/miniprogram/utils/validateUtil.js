// utils/validateUtil.js

const validateUtil = {
  /**
   * 校验手机号
   */
  isPhone: (phone) => {
    const reg = /^1[3-9]\d{9}$/;
    return reg.test(phone);
  },

  /**
   * 校验身份证号 (简单校验位数)
   * 严格校验需结合加权因子，此处配合后端或OCR使用
   */
  isIdCard: (idCard) => {
    const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
    return reg.test(idCard);
  },

  /**
   * 校验金额 (非负数，最多两位小数)
   */
  isMoney: (amount) => {
    const reg = /^(([1-9][0-9]*)|(([0]\.\d{1,2}|[1-9][0-9]*\.\d{1,2})))$/;
    return reg.test(amount);
  },

  /**
   * 判空校验
   */
  isEmpty: (value) => {
    return value === null || value === undefined || value === '';
  }
};

module.exports = validateUtil;