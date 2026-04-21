const crypto = require('crypto');
const { exec } = require('child_process');
const util = require('util');
const execPromise = util.promisify(exec);

// 计算admin123的MD5
const newPassword = 'admin123';
const md5Hash = crypto.createHash('md5').update(newPassword).digest('hex');

console.log(`新密码: ${newPassword}`);
console.log(`MD5值: ${md5Hash}`);

// 更新数据库
async function updateDatabase() {
  const updateSql = `USE campus_tutor_db; UPDATE sys_user SET password = '${md5Hash}' WHERE username = 'admin';`;
  const command = `mysql -u root -p200512 -e "${updateSql}"`;
  
  console.log('执行SQL更新...');
  
  try {
    const { stdout, stderr } = await execPromise(command);
    if (stderr && !stderr.includes('Warning')) {
      console.error('更新失败:', stderr);
    } else {
      console.log('✅ 管理员密码更新成功！');
      console.log(`管理员账户: admin`);
      console.log(`新密码: ${newPassword}`);
      console.log(`MD5哈希: ${md5Hash}`);
      console.log('\n现在可以尝试使用以下凭据登录管理端：');
      console.log('用户名: admin');
      console.log('密码: admin123');
    }
  } catch (error) {
    console.error('执行失败:', error.message);
  }
}

// 验证原密码
function checkOriginalPassword() {
  const originalHash = '47ec2dd791e31e2ef2076caf64ed9b3d';
  const testPasswords = ['admin', '123456', 'password', 'root', 'admin123'];
  
  console.log('验证原密码...');
  for (const pwd of testPasswords) {
    const hash = crypto.createHash('md5').update(pwd).digest('hex');
    if (hash === originalHash) {
      console.log(`原密码可能是: "${pwd}" (MD5: ${hash})`);
      break;
    }
  }
}

// 运行
checkOriginalPassword();
console.log('\n---\n');
updateDatabase();