const crypto = require('crypto');

function md5(text) {
  return crypto.createHash('md5').update(text).digest('hex');
}

function bcryptCheck(password, hash) {
  // 简单的BCrypt检查（实际使用时需要bcrypt库）
  return hash.startsWith('$2') && password === 'admin'; // 简化检查
}

const storedPassword = '47ec2dd791e31e2ef2076caf64ed9b3d';
const testPasswords = ['admin', '123456', 'password', 'root', 'admin123'];

console.log('测试常用密码的MD5值:');
console.log('存储的MD5:', storedPassword);
console.log('-------------------');

for (const pwd of testPasswords) {
  const md5Hash = md5(pwd);
  console.log(`密码: "${pwd}"`);
  console.log(`MD5: ${md5Hash}`);
  console.log(`匹配: ${md5Hash === storedPassword ? '✅' : '❌'}`);
  console.log('-------------------');
}