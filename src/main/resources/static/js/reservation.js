const API = '/admin/reservations';

document.addEventListener('DOMContentLoaded', () => {
    refresh();
});

async function refresh() {
    try {
        const data = await fetchJson(API);
        render(data.reservations);
    } catch (error) {
        console.error('예약 조회 실패:', error);
    }
}

function render(reservations) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';

    if (reservations.length === 0) {
        showEmptyState(tbody, 6, '등록된 예약이 없습니다.');
        return;
    }

    reservations.forEach((reservation, index) => {
        const row = tbody.insertRow();

        row.insertCell().textContent = index + 1;
        row.insertCell().textContent = reservation.name;
        row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
        row.insertCell().textContent = reservation.date;
        row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';

        const actions = row.insertCell();
        actions.className = 'actions';
        actions.appendChild(createButton('삭제', 'btn-danger', () => deleteRow(reservation.id)));
    });
}

async function deleteRow(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await fetchJson(`${API}/${id}`, { method: 'DELETE' });
        refresh();
    } catch (error) {
        console.error('예약 삭제 실패:', error);
        alert(getErrorMessage(error, '예약 삭제에 실패했습니다.'));
    }
}
