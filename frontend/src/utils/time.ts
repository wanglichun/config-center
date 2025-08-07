/**
 * 时间处理工具类
 * 提供各种时间格式化、转换功能
 */
export const TimeUtils = {
  /**
   * 格式化时间为指定格式
   * @param {string | number | Date} time - 要格式化的时间，可以是时间戳、日期字符串或Date对象
   * @param {string} [format] - 自定义格式，例如 'yyyy-MM-dd HH:mm:ss'
   * @param {string} [locale='zh-CN'] - 本地化语言，默认中文
   * @returns {string} 格式化后的时间字符串，无效时间返回 '-'
   */
  formatTime(
      time?: string | number | Date,
      format?: string,
      locale: string = 'zh-CN'
  ): string {
    if (!time) return '-'

    // 尝试将输入转换为Date对象
    let date: Date | null = null
    if (time instanceof Date) {
      date = time
    } else {
      // 处理时间戳（支持秒级和毫秒级）
      if (typeof time === 'number') {
        // 如果是秒级时间戳（位数小于13位），转换为毫秒级
        if (time.toString().length < 13) {
          time *= 1000
        }
        date = new Date(time)
      } else if (typeof time === 'string') {
        date = new Date(time)
      }
    }

    // 检查是否为有效日期
    if (!date || isNaN(date.getTime())) {
      return '-'
    }

    // 如果指定了格式，使用自定义格式化
    if (format) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')

      return format
          .replace('yyyy', year.toString())
          .replace('MM', month)
          .replace('dd', day)
          .replace('HH', hours)
          .replace('mm', minutes)
          .replace('ss', seconds)
    }

    // 否则使用本地化格式
    return date.toLocaleString(locale)
  },

  /**
   * 获取当前时间戳（毫秒级）
   * @returns {number} 当前时间戳
   */
  getCurrentTimestamp(): number {
    return Date.now()
  },

  /**
   * 获取当前日期的格式化字符串
   * @param {string} [format='yyyy-MM-dd'] - 日期格式
   * @returns {string} 格式化后的日期字符串
   */
  getCurrentDate(format: string = 'yyyy-MM-dd'): string {
    return this.formatTime(new Date(), format)
  },

  /**
   * 获取当前时间的格式化字符串
   * @param {string} [format='HH:mm:ss'] - 时间格式
   * @returns {string} 格式化后的时间字符串
   */
  getCurrentTime(format: string = 'HH:mm:ss'): string {
    return this.formatTime(new Date(), format)
  }
}

// 使用示例
// console.log(TimeUtils.formatTime(1620000000000))
// console.log(TimeUtils.getCurrentDate())
// console.log(TimeUtils.getCurrentTime())
// console.log(TimeUtils.formatTime(new Date(), 'yyyy年MM月dd日 HH:mm:ss'))