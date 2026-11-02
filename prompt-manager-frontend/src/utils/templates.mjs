export function templateVariables(template) {
  return [...new Set([...template.matchAll(/\{\{\s*([^{}]+?)\s*\}\}/g)].map(match => match[1].trim()))]
}
export function previewTemplate(template, values) {
  return template.replace(/\{\{\s*([^{}]+?)\s*\}\}/g, (placeholder, name) =>
    Object.hasOwn(values, name.trim()) ? values[name.trim()] ?? placeholder : placeholder)
}
export function decodeTemplate(buffer) {
  const bytes = new Uint8Array(buffer)
  if (bytes.length > 100000) throw new Error('文件最多 100 KB')
  if ((bytes[0] === 0xff && bytes[1] === 0xfe) || (bytes[0] === 0xfe && bytes[1] === 0xff))
    throw new Error('请先将 UTF-16 文件另存为 UTF-8')
  let text
  try { text = new TextDecoder('utf-8', { fatal: true }).decode(buffer) }
  catch { text = new TextDecoder('gb18030', { fatal: true }).decode(buffer) }
  if (text.includes('\0')) throw new Error('文件包含非文本内容')
  if (text.length > 20000) throw new Error('模板最多 20000 字')
  return text
}
