const API = '/reservations/me';
const RESERVATIONS_API = '/reservations';
const TIMES_API = '/times';
const THEMES_API = '/themes';

let isEditing = false;
let cachedThemes = [];

const dialog = document.getElementById('add-dialog');
const themeSelect = document.getElementById('add-theme');
const dateInput = document.getElementById('add-date');
const timeSelect = document.getElementById('add-time');

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', openAddDialog);
    document.getElementById('add-close').addEventListener('click', closeAddDialog);
    document.getElementById('add-cancel').addEventListener('click', closeAddDialog);
    document.getElementById('add-save').addEventListener('click', saveAdd);
    themeSelect.addEventListener('change', () => refreshAvailableTimesForAdd());
    dateInput.addEventListener('change', () => refreshAvailableTimesForAdd());
    refresh();
});

async function refresh() {
    try {
        const [reservationData, themeData] = await Promise.all([
            fetchJson(API),
            fetchJson(THEMES_API)
        ]);
        cachedThemes = themeData.themes;
        render(reservationData.reservations);
    } catch (error) {
        if (error.status === 401) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login?redirect=' + encodeURIComponent(location.pathname);
            return;
        }
        console.error('내 예약 조회 실패:', error);
        alert(getErrorMessage(error, '내 예약 조회에 실패했습니다.'));
    }
}

function render(reservations) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';
    isEditing = false;

    if (reservations.length === 0) {
        showEmptyState(tbody, 5, '예약 내역이 없습니다. "+ 예약 추가" 버튼으로 시작해보세요.');
        return;
    }

    reservations.forEach((reservation, index) => {
        renderRow(tbody, reservation, index);
    });
}

function renderRow(tbody, reservation, index) {
    const row = tbody.insertRow();
    row.insertCell().textContent = index + 1;
    row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
    row.insertCell().textContent = reservation.date;
    row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';

    const actions = row.insertCell();
    actions.className = 'actions';
    actions.appendChild(createButton('변경', 'btn-primary', () => startEdit(row, reservation)));
    actions.appendChild(createButton('취소', 'btn-ghost', () => cancelReservation(reservation.id)));
}

function openAddDialog() {
    if (cachedThemes.length === 0) {
        alert('등록된 테마가 없습니다.');
        return;
    }

    clearSelect(themeSelect);
    appendPlaceholder(themeSelect, '테마를 선택해주세요.');
    cachedThemes.forEach(theme => {
        const option = document.createElement('option');
        option.value = theme.id;
        option.textContent = theme.name;
        themeSelect.appendChild(option);
    });

    dateInput.value = '';

    clearSelect(timeSelect);
    appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
    timeSelect.disabled = true;

    dialog.showModal();
}

function closeAddDialog() {
    dialog.close();
}

async function refreshAvailableTimesForAdd() {
    const date = dateInput.value;
    const themeId = themeSelect.value;
    clearSelect(timeSelect);

    if (!date || !themeId) {
        appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
        timeSelect.disabled = true;
        return;
    }

    try {
        const data = await fetchJson(
            `${TIMES_API}/availability?date=${encodeURIComponent(date)}&themeId=${encodeURIComponent(themeId)}`
        );
        const times = data.times;

        if (!times || times.length === 0) {
            appendPlaceholder(timeSelect, '등록된 시간이 없습니다.');
            timeSelect.disabled = true;
            return;
        }

        let hasAvailableTime = false;

        times.forEach(time => {
            const option = document.createElement('option');
            const reserved = time.reserved === true;

            option.value = time.id;
            option.textContent = reserved ? `${time.startAt} (예약 불가)` : time.startAt;
            option.disabled = reserved;

            if (!reserved && !hasAvailableTime) {
                option.selected = true;
                hasAvailableTime = true;
            }

            timeSelect.appendChild(option);
        });

        timeSelect.disabled = !hasAvailableTime;

        if (!hasAvailableTime) {
            alert('선택한 날짜와 테마에 예약 가능한 시간이 없습니다.');
        }
    } catch (error) {
        console.error('예약 가능 시간 조회 실패:', error);
        appendPlaceholder(timeSelect, '예약 가능 시간 조회 실패');
        timeSelect.disabled = true;
    }
}

async function saveAdd() {
    const date = dateInput.value;
    const themeId = themeSelect.value;
    const timeId = timeSelect.value;

    if (!date || !themeId || !timeId) {
        alert('테마·날짜·시간을 모두 선택해주세요.');
        return;
    }

    const selectedOption = timeSelect.options[timeSelect.selectedIndex];
    if (selectedOption && selectedOption.disabled) {
        alert('이미 예약된 시간은 선택할 수 없습니다.');
        return;
    }

    try {
        await fetchJson(RESERVATIONS_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                date,
                themeId: Number(themeId),
                timeId: Number(timeId)
            })
        });
        closeAddDialog();
        await refresh();
    } catch (error) {
        if (error.status === 401) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login?redirect=' + encodeURIComponent(location.pathname);
            return;
        }
        console.error('예약 추가 실패:', error);
        alert(getErrorMessage(error, '예약 추가에 실패했습니다.'));
    }
}

function startEdit(row, reservation) {
    if (isEditing) return;
    if (!reservation.theme || !reservation.time) {
        alert('예약 정보가 올바르지 않아 변경할 수 없습니다.');
        return;
    }

    isEditing = true;

    const dateCell = row.cells[2];
    const timeCell = row.cells[3];
    const actions = row.cells[4];

    const editDate = createInput('date');
    editDate.value = reservation.date;

    const editTime = createSelect();

    dateCell.innerHTML = '';
    dateCell.appendChild(editDate);
    timeCell.innerHTML = '';
    timeCell.appendChild(editTime);

    refreshAvailableTimes(editDate.value, reservation.theme.id, editTime, reservation.time.id);

    editDate.addEventListener('change', () => {
        refreshAvailableTimes(editDate.value, reservation.theme.id, editTime, reservation.time.id);
    });

    actions.innerHTML = '';
    actions.appendChild(createButton('저장', 'btn-primary', () => {
        saveEdit(reservation.id, editDate.value, editTime.value);
    }));
    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        refresh();
    }));
}

async function refreshAvailableTimes(date, themeId, select, currentTimeId) {
    clearSelect(select);

    if (!date || !themeId) {
        appendPlaceholder(select, '날짜를 먼저 선택해주세요.');
        select.disabled = true;
        return;
    }

    try {
        const data = await fetchJson(
            `${TIMES_API}/availability?date=${encodeURIComponent(date)}&themeId=${encodeURIComponent(themeId)}`
        );
        const times = data.times;

        clearSelect(select);

        if (!times || times.length === 0) {
            appendPlaceholder(select, '등록된 시간이 없습니다.');
            select.disabled = true;
            return;
        }

        let hasSelected = false;

        times.forEach(time => {
            const option = document.createElement('option');
            const isCurrent = String(time.id) === String(currentTimeId);
            const reserved = time.reserved === true && !isCurrent;

            option.value = time.id;
            option.textContent = reserved ? `${time.startAt} (예약 불가)` : time.startAt;
            option.disabled = reserved;

            if (isCurrent) {
                option.selected = true;
                hasSelected = true;
            } else if (!reserved && !hasSelected) {
                option.selected = true;
                hasSelected = true;
            }

            select.appendChild(option);
        });

        select.disabled = !hasSelected;
    } catch (error) {
        console.error('예약 가능 시간 조회 실패:', error);
        clearSelect(select);
        appendPlaceholder(select, '예약 가능 시간 조회 실패');
        select.disabled = true;
    }
}

async function saveEdit(id, date, timeId) {
    if (!date || !timeId) {
        alert('날짜와 시간을 모두 선택해주세요.');
        return;
    }

    try {
        await fetchJson(`${API}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ date, timeId: Number(timeId) })
        });
        await refresh();
    } catch (error) {
        if (error.status === 401) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login?redirect=' + encodeURIComponent(location.pathname);
            return;
        }
        console.error('예약 변경 실패:', error);
        alert(getErrorMessage(error, '예약 변경에 실패했습니다.'));
    }
}

async function cancelReservation(id) {
    if (!confirm('예약을 취소하시겠습니까?')) return;

    try {
        await fetchJson(`${API}/${id}`, { method: 'DELETE' });
        await refresh();
    } catch (error) {
        if (error.status === 401) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login?redirect=' + encodeURIComponent(location.pathname);
            return;
        }
        console.error('예약 취소 실패:', error);
        alert(getErrorMessage(error, '예약 취소에 실패했습니다.'));
    }
}

function appendPlaceholder(select, message) {
    const option = document.createElement('option');
    option.value = '';
    option.textContent = message;
    select.appendChild(option);
}

function clearSelect(select) {
    select.innerHTML = '';
}
