export const formatValue = (value: any, placeholder = '-'): any => {
  if (value === null || value === undefined || value === '') {
    return placeholder
  }
  return value
}

export const formatDate = (value: any, placeholder = '-'): string => {
  if (!value) return placeholder
  if (typeof value === 'string') {
    try {
      const date = new Date(value)
      if (isNaN(date.getTime())) return placeholder
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    } catch {
      return placeholder
    }
  }
  return placeholder
}
