function createInput(type = 'text', placeholder = '') {
    const input = document.createElement('input');
    input.type = type;
    input.className = 'input';
    if (placeholder) input.placeholder = placeholder;
    return input;
}

function createSelect() {
    const select = document.createElement('select');
    select.className = 'select';
    return select;
}

function createButton(label, variant, onClick) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `btn ${variant}`;
    button.textContent = label;
    button.addEventListener('click', onClick);
    return button;
}

function showEmptyState(tbody, colspan, message) {
    const tr = tbody.insertRow();
    const td = tr.insertCell();
    td.colSpan = colspan;
    td.className = 'empty-state';
    td.textContent = message;
}

class HttpError extends Error {
    constructor(response, problem) {
        super(problem?.detail || `요청이 실패했습니다. (HTTP ${response.status})`);
        this.name = 'HttpError';
        this.status = response.status;
        this.problem = problem;
    }
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let problem = null;
        const contentType = response.headers.get('Content-Type') || '';
        if (contentType.includes('application/problem+json')) {
            try {
                problem = await response.json();
            } catch (_) {
                // problem body 파싱 실패 시 무시하고 status만으로 진행
            }
        }
        throw new HttpError(response, problem);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

async function initAuthNav() {
    const container = document.getElementById('nav-auth');
    if (!container) return;

    const response = await fetch('/members/me');
    container.innerHTML = '';

    if (response.ok) {
        const me = await response.json();
        const greeting = document.createElement('span');
        greeting.className = 'nav-user';
        greeting.textContent = `${me.name}님`;

        const logoutButton = document.createElement('button');
        logoutButton.type = 'button';
        logoutButton.className = 'btn btn-ghost btn-sm';
        logoutButton.textContent = '로그아웃';
        logoutButton.addEventListener('click', logout);

        container.appendChild(greeting);
        container.appendChild(logoutButton);
        return;
    }

    const loginLink = document.createElement('a');
    loginLink.href = '/login';
    loginLink.className = 'btn btn-primary btn-sm';
    loginLink.textContent = '로그인';
    container.appendChild(loginLink);
}

async function logout() {
    try {
        await fetch('/login/sessions', { method: 'DELETE' });
    } catch (error) {
        console.error('로그아웃 실패:', error);
    }
    location.href = '/';
}

document.addEventListener('DOMContentLoaded', initAuthNav);
document.addEventListener('DOMContentLoaded', showAccessErrorIfAny);

function showAccessErrorIfAny() {
    const params = new URLSearchParams(location.search);
    const error = params.get('error');
    if (!error) return;

    if (error === 'admin_only') {
        alert('관리자 권한이 필요한 페이지입니다.');
    }

    params.delete('error');
    const remaining = params.toString();
    const newUrl = location.pathname + (remaining ? `?${remaining}` : '') + location.hash;
    history.replaceState(null, '', newUrl);
}

function getErrorMessage(error, fallback) {
    if (error instanceof HttpError) {
        const problem = error.problem;
        if (problem && Array.isArray(problem.errors) && problem.errors.length > 0) {
            const joined = problem.errors
                .map(item => item.reason)
                .filter(Boolean)
                .join('\n');
            if (joined) return joined;
        }
        if (problem && problem.detail) return problem.detail;
    }
    return fallback;
}
