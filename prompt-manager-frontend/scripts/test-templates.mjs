import assert from 'node:assert/strict'
import { templateVariables, previewTemplate, decodeTemplate } from '../src/utils/templates.mjs'
assert.deepEqual(templateVariables('{{ a.b }} {{名字}} {{a.b}}'), ['a.b', '名字'])
assert.equal(previewTemplate('{{constructor}} {{__proto__}}', {}), '{{constructor}} {{__proto__}}')
const special = Object.create(null)
special.constructor = '构造说明'
special.__proto__ = '原型说明'
assert.equal(previewTemplate('{{constructor}} {{__proto__}}', special), '构造说明 原型说明')
assert.equal(JSON.parse(JSON.stringify({ ...special })).__proto__, '原型说明')
assert.equal(previewTemplate('{{ a.b }} {{名字}}', { 'a.b': '$&\\x{{名字}}', 名字: '李萌' }), '$&\\x{{名字}} 李萌')
assert.equal(decodeTemplate(new TextEncoder().encode('合法 UTF-8 � 中文').buffer), '合法 UTF-8 � 中文')
assert.equal(decodeTemplate(Uint8Array.from([0xd6, 0xd0, 0xce, 0xc4]).buffer), '中文')
assert.throws(() => decodeTemplate(Uint8Array.from([0xff, 0xfe, 0x61, 0]).buffer), /UTF-16/)
assert.throws(() => decodeTemplate(new TextEncoder().encode('x'.repeat(20001)).buffer), /20000/)
console.log('Template substitution and encoding tests passed')
