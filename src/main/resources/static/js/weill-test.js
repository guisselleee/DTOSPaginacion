(() => {
    const shell = document.querySelector('.test-shell');
    if (!shell) {
        return;
    }

    const attemptId = shell.dataset.attemptId;
    const totalItems = Number(shell.dataset.totalItems);
    const selections = new Map();
    const answeredCount = document.querySelector('[data-answered-count]');
    const statusMessage = document.querySelector('[data-status-message]');
    const saveButton = document.querySelector('[data-save-progress]');
    const finishButton = document.querySelector('[data-finish-test]');
    let dragState = null;
    let animationFrame = 0;

    document.addEventListener('pointerdown', beginDrag);
    document.addEventListener('pointermove', moveDrag);
    document.addEventListener('pointerup', endDrag);
    document.addEventListener('pointercancel', cancelDrag);
    saveButton.addEventListener('click', saveProgress);
    finishButton.addEventListener('click', finishTest);

    function beginDrag(event) {
        const tile = event.target.closest('.option-tile');
        if (!tile || !shell.contains(tile)) {
            return;
        }
        event.preventDefault();
        const item = tile.closest('.test-item');
        dragState = {
            tile,
            item,
            option: Number(tile.dataset.option),
            x: event.clientX,
            y: event.clientY,
            ghost: createGhost(tile)
        };
        document.body.appendChild(dragState.ghost);
        positionGhost();
    }

    function moveDrag(event) {
        if (!dragState) {
            return;
        }
        dragState.x = event.clientX;
        dragState.y = event.clientY;
        if (!animationFrame) {
            animationFrame = requestAnimationFrame(() => {
                animationFrame = 0;
                positionGhost();
                updateHotDropZone();
            });
        }
    }

    function endDrag(event) {
        if (!dragState) {
            return;
        }
        const dropZone = dropZoneAt(event.clientX, event.clientY);
        if (dropZone && dropZone.closest('.test-item') === dragState.item) {
            selectOption(dragState.item, dragState.option);
        }
        cleanupDrag();
    }

    function cancelDrag() {
        cleanupDrag();
    }

    function createGhost(tile) {
        const ghost = tile.cloneNode(true);
        ghost.className = 'drag-ghost';
        ghost.removeAttribute('type');
        return ghost;
    }

    function positionGhost() {
        if (!dragState) {
            return;
        }
        dragState.ghost.style.transform = `translate3d(${dragState.x + 12}px, ${dragState.y + 12}px, 0)`;
    }

    function updateHotDropZone() {
        document.querySelectorAll('.drop-zone.is-hot').forEach(zone => zone.classList.remove('is-hot'));
        if (!dragState) {
            return;
        }
        const dropZone = dropZoneAt(dragState.x, dragState.y);
        if (dropZone && dropZone.closest('.test-item') === dragState.item) {
            dropZone.classList.add('is-hot');
        }
    }

    function dropZoneAt(x, y) {
        const previousDisplay = dragState?.ghost?.style.display;
        if (dragState?.ghost) {
            dragState.ghost.style.display = 'none';
        }
        const element = document.elementFromPoint(x, y);
        if (dragState?.ghost) {
            dragState.ghost.style.display = previousDisplay || '';
        }
        return element?.closest?.('[data-dropzone]') || null;
    }

    function cleanupDrag() {
        if (animationFrame) {
            cancelAnimationFrame(animationFrame);
            animationFrame = 0;
        }
        document.querySelectorAll('.drop-zone.is-hot').forEach(zone => zone.classList.remove('is-hot'));
        dragState?.ghost?.remove();
        dragState = null;
    }

    function selectOption(item, option) {
        const key = itemKey(item);
        selections.set(key, {
            pageNumber: Number(item.dataset.page),
            rowNumber: Number(item.dataset.row),
            itemNumber: Number(item.dataset.item),
            selectedOption: option
        });
        item.querySelectorAll('.option-tile').forEach(tile => {
            tile.classList.toggle('is-selected', Number(tile.dataset.option) === option);
        });
        item.querySelector('[data-dropzone]').classList.add('has-answer');
        item.querySelector('[data-selection-label]').textContent = `Opción ${option}`;
        updateProgress();
    }

    function itemKey(item) {
        return `${item.dataset.page}:${item.dataset.row}:${item.dataset.item}`;
    }

    function updateProgress() {
        answeredCount.textContent = selections.size;
    }

    async function saveProgress() {
        if (selections.size === 0) {
            setStatus('No hay respuestas para guardar todavía.');
            return;
        }
        const result = await postResponses(`/api/attempts/${attemptId}/responses`);
        setStatus(`${result.answeredItems}/${result.totalItems} respuestas guardadas.`);
    }

    async function finishTest() {
        if (selections.size < totalItems) {
            const missing = totalItems - selections.size;
            setStatus(`Faltan ${missing} respuestas antes de finalizar.`);
            return;
        }
        const result = await postResponses(`/api/attempts/${attemptId}/complete`);
        window.location.assign(result.resultsUrl);
    }

    async function postResponses(url) {
        setBusy(true);
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({responses: Array.from(selections.values())})
            });
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            setStatus('No se pudo guardar. Revisa la conexión e intenta de nuevo.');
            throw error;
        } finally {
            setBusy(false);
        }
    }

    function setBusy(busy) {
        saveButton.disabled = busy;
        finishButton.disabled = busy;
    }

    function setStatus(message) {
        statusMessage.textContent = message;
    }
})();
