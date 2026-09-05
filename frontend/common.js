// ========== common.js（美化版） ==========
// 说明：在原有功能基础上增加 Toast 提示、渐变导航栏与品牌 Logo；
//       所有接口约定不变：后端返回 {code:200, message:"success", data:...}

// ---------- Toast 提示：替换原生 alert（confirm/prompt 保留） ----------
(function () {
  const wrap = document.createElement('div');
  wrap.className = 'yz-toast-wrap';
  document.body.appendChild(wrap);

  window.showToast = function (msg, type) {
    const t = document.createElement('div');
    t.className = 'yz-toast ' + (type || 'info');
    t.textContent = msg;
    wrap.appendChild(t);
    requestAnimationFrame(() => t.classList.add('show'));
    setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 320); }, 2600);
  };

  // 全局把 alert 替换为 toast，原有业务代码无需改动
  window.alert = function (msg) { window.showToast(msg, 'info'); };
})();

// 1. 统一 fetch 封装：自动带 token、统一错误提示、401 自动跳登录页
async function api(url, options = {}) {
  const token = localStorage.getItem('token');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const resp = await fetch(url, { ...options, headers });
  if (resp.status === 401) {
    localStorage.removeItem('token');
    location.href = 'login.html';
    throw new Error('未登录');
  }
  const data = await resp.json().catch(() => ({}));
  if (!resp.ok || data.code !== 200) {
    showToast(data.message || ('请求失败(' + resp.status + ')'), 'error');
    throw new Error(data.message || resp.status);
  }
  return data;
}

// 2. 文件上传（multipart/form-data，不能用 api 封装的 JSON 头）
async function uploadFile(file) {
  const fd = new FormData();
  fd.append('file', file);
  const headers = {};
  const token = localStorage.getItem('token');
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const resp = await fetch('/api/upload', { method: 'POST', body: fd, headers });
  const data = await resp.json().catch(() => ({}));
  if (!resp.ok || data.code !== 200) {
    showToast(data.message || '上传失败', 'error');
    throw new Error('上传失败');
  }
  return data.data.url;
}

// 3. 读取 URL 查询参数
function getQuery(name) {
  return new URLSearchParams(location.search).get(name);
}

// 4. 品牌 Logo（内联 SVG，离线可用）
function brandLogo() {
  return '<span class="brand-logo">' +
    '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' +
    '<path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z"></path>' +
    '<path d="M3 6h18"></path><path d="M16 10a4 4 0 0 1-8 0"></path>' +
    '</svg></span>';
}

// 5. 统一渲染顶部导航栏：links = [{text, href}]
function renderNav(brand, links = []) {
  const nav = document.querySelector('nav');
  nav.classList.add('yz-nav');
  const logged = !!localStorage.getItem('token');
  const isAdmin = localStorage.getItem('role') === 'ADMIN';
  const right = links.map(l => `<a class="btn btn-outline-light btn-sm me-2" href="${l.href}">${l.text}</a>`).join('');
  const adminBtn = (logged && isAdmin) ? '<a class="btn btn-outline-warning btn-sm me-2" href="admin.html">管理后台</a>' : '';
  const userBtns = logged
    ? '<a class="btn btn-outline-light btn-sm me-2" href="orders.html">我的订单</a><button class="btn btn-outline-danger btn-sm" onclick="logout()">退出</button>'
    : '<a class="btn btn-outline-light btn-sm" href="login.html">登录</a>';
  nav.innerHTML = '<div class="container-fluid">'
    + '<a class="navbar-brand" href="index.html">' + brandLogo() + brand + '</a>'
    + '<div>' + right + adminBtn + userBtns + '</div>'
    + '</div>';
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  localStorage.removeItem('username');
  location.href = 'login.html';
}

// 6. 统一渲染表格：cols = [{key, title}]，opsHtml(item) 生成每行操作列（可省略）
function renderTable(tableId, list, cols, opsHtml) {
  const thead = '<tr>' + cols.map(c => '<th>' + c.title + '</th>').join('')
    + (opsHtml ? '<th>操作</th>' : '') + '</tr>';
  const tbody = list.map(item =>
    '<tr>' + cols.map(c => '<td>' + (item[c.key] ?? '-') + '</td>').join('')
    + (opsHtml ? opsHtml(item) : '') + '</tr>').join('');
  document.querySelector(tableId + ' thead').innerHTML = thead;
  document.querySelector(tableId + ' tbody').innerHTML = tbody || '<tr><td colspan="99"><div class="yz-empty"><span class="emoji">📭</span>暂无数据</div></td></tr>';
}

// 7. 状态徽章
function goodsBadge(status) {
  const map = { 0: ['text-bg-secondary', '待审核'], 1: ['text-bg-success', '上架中'], 2: ['text-bg-danger', '已下架'] };
  const m = map[status] || ['text-bg-dark', '未知'];
  return '<span class="badge ' + m[0] + '">' + m[1] + '</span>';
}

function orderBadge(status) {
  const map = { 0: ['text-bg-warning', '待支付'], 1: ['text-bg-info', '已支付'], 2: ['text-bg-success', '已完成'] };
  const m = map[status] || ['text-bg-dark', '未知'];
  return '<span class="badge ' + m[0] + '">' + m[1] + '</span>';
}
