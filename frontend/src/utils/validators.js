const FIELD_LIMITS = {
  subject: 255,
  description: 500,
  customerName: 120,
  customerEmail: 254,
  customerPhone: 10,
}

/**
 * Email validator for ticket creation form
 * Requires Latin characters and valid email format
 */
export const emailLatinValidator = (_, value, callback) => {
  const normalized = String(value || '').trim()
  const latinEmailPattern = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/

  if (!normalized) {
    callback(new Error('Введите email клиента'))
    return
  }

  if (!latinEmailPattern.test(normalized)) {
    callback(new Error('Email должен быть на латинице и в корректном формате'))
    return
  }

  if (normalized.length > FIELD_LIMITS.customerEmail) {
    callback(new Error(`Email не должен превышать ${FIELD_LIMITS.customerEmail} символа`))
    return
  }

  callback()
}

/**
 * Phone validator for ticket creation form
 * Requires exactly 10 digits (Russian phone format: +7XXXXXXXXXX)
 */
export const phoneValidator = (_, value, callback) => {
  const digits = String(value || '').replace(/\D/g, '')

  if (!digits) {
    callback(new Error('Введите телефон'))
    return
  }

  if (digits.length !== 10) {
    callback(new Error('Введите 10 цифр номера после кода +7'))
    return
  }

  callback()
}

/**
 * Normalize phone input to digits only
 */
export const normalizePhoneInput = (value) => {
  return String(value || '')
    .replace(/\D/g, '')
    .slice(0, 10)
}

/**
 * Format phone number with +7 prefix
 */
export const formatPhoneWithPrefix = (digits) => `+7${digits}`

export { FIELD_LIMITS }
