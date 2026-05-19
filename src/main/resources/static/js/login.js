const form = document.getElementById('login-form');
const errorEl = document.getElementById('error-message');
const submitButton = form.querySelector('button[type="submit"]');

form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorEl.textContent = '';
    submitButton.disabled = true;

    const formData = new FormData(e.target);
    const body = {
        email: formData.get('email'),
        password: formData.get('password'),
    };

    try {
        const res = await fetch('/login/sessions', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body),
        });

        if (res.ok) {
            const redirect = new URLSearchParams(location.search).get('redirect') || '/';
            location.href = redirect;
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
    } finally {
        submitButton.disabled = false;
    }
});
