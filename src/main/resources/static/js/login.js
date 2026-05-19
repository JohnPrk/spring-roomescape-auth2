document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const body = {
        email: formData.get('email'),
        password: formData.get('password'),
    };

    const res = await fetch('/login/sessions', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body),
    });

    const errorEl = document.getElementById('error-message');
    if (res.ok) {
        errorEl.textContent = '';
        location.href = '/';
        return;
    }

    let message = '로그인에 실패했습니다.';
    try {
        const problem = await res.json();
        if (problem && problem.detail) {
            message = problem.detail;
        }
    } catch (_) {
    }
    errorEl.textContent = message;
});
