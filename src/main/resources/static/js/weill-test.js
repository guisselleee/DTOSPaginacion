(() => {
    const shell = document.querySelector('.test-shell');
    if (!shell) {
        return;
    }

    const attemptId = shell.dataset.attemptId;
    const totalItems = Number(shell.dataset.totalItems);
    const items = Array.from(document.querySelectorAll('[data-test-items] .test-item'));
    const selections = new Map();
    const answeredCount = document.querySelector('[data-answered-count]');
    const correctCount = document.querySelector('[data-correct-count]');
    const elapsedTime = document.querySelector('[data-elapsed-time]');
    const currentItem = document.querySelector('[data-current-item]');
    const currentTitle = document.querySelector('[data-current-title]');
    const statusMessage = document.querySelector('[data-status-message]');
    const finishButton = document.querySelector('[data-finish-test]');
    let dragState = null;
    let animationFrame = 0;
    let activeIndex = 0;
    let busy = false;

    document.querySelectorAll('.option-tile img').forEach(image => image.draggable = false);
    startElapsedTimer();
    showActiveItem();
    document.addEventListener('pointerdown', beginDrag);
    document.addEventListener('pointermove', moveDrag);
    document.addEventListener('pointerup', endDrag);
    document.addEventListener('pointercancel', cancelDrag);
    finishButton.addEventListener('click', finishTest);
    finishButton.disabled = true;

    function beginDrag(event) {
        const tile = event.target.closest('.option-tile');
        if (busy || !tile || !shell.contains(tile)) {
            return;
        }
        event.preventDefault();
        tile.setPointerCapture?.(event.pointerId);
        const item = tile.closest('.test-item');
        if (!item?.classList.contains('is-active')) {
            return;
        }
        dragState = {
            tile,
            item,
            option: Number(tile.dataset.option),
            imageSrc: tile.querySelector('img')?.currentSrc || tile.querySelector('img')?.src,
            x: event.clientX,
            y: event.clientY,
            ghost: createGhost(tile)
        };
        tile.classList.add('is-dragging');
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
            void selectOption(dragState.item, dragState.option, dragState.imageSrc);
        }
        cleanupDrag();
    }

    function cancelDrag() {
        cleanupDrag();
    }

    function createGhost(tile) {
        const ghost = document.createElement('div');
        ghost.className = 'drag-ghost is-visible';
        ghost.setAttribute('aria-hidden', 'true');

        const image = tile.querySelector('img')?.cloneNode(true);
        if (image) {
            image.draggable = false;
            ghost.appendChild(image);
        }
        return ghost;
    }

    function positionGhost() {
        if (!dragState) {
            return;
        }
        dragState.ghost.style.left = (dragState.x + 14) + 'px';
        dragState.ghost.style.top = (dragState.y + 14) + 'px';
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
        dragState?.tile?.classList.remove('is-dragging');
        dragState?.ghost?.remove();
        dragState = null;
    }

    async function selectOption(item, option, imageSrc) {
        const key = itemKey(item);
        const response = {
            pageNumber: Number(item.dataset.page),
            rowNumber: Number(item.dataset.row),
            itemNumber: Number(item.dataset.item),
            selectedOption: option
        };
        item.querySelectorAll('.option-tile').forEach(tile => {
            tile.classList.toggle('is-selected', Number(tile.dataset.option) === option);
        });
        renderPlacedAnswer(item.querySelector('[data-dropzone]'), option, imageSrc);
        item.querySelector('[data-selection-label]').textContent = `Opción ${option}`;
        setStatus('Guardando respuesta...');

        try {
            const result = await postResponses(`/api/attempts/${attemptId}/responses`, [response]);
            selections.set(key, response);
            updateProgress(result);
            advanceAfterSave();
        } catch (error) {
            clearPlacedAnswer(item);
            updateProgress();
            setStatus('No se pudo guardar la respuesta. Intenta arrastrarla de nuevo.');
        }
    }

    function renderPlacedAnswer(dropZone, option, imageSrc) {
        dropZone.classList.add('has-answer');
        dropZone.replaceChildren();

        const image = document.createElement('img');
        image.className = 'placed-answer-image';
        image.src = imageSrc;
        image.alt = `Opción ${option} seleccionada`;
        image.draggable = false;

        dropZone.append(image);
    }

    function clearPlacedAnswer(item) {
        item.querySelectorAll('.option-tile').forEach(tile => tile.classList.remove('is-selected'));
        const dropZone = item.querySelector('[data-dropzone]');
        dropZone.classList.remove('has-answer');
        dropZone.replaceChildren();
        item.querySelector('[data-selection-label]').textContent = 'Sin respuesta';
    }

    function itemKey(item) {
        return `${item.dataset.page}:${item.dataset.row}:${item.dataset.item}`;
    }

    function updateProgress(result) {
        answeredCount.textContent = result?.answeredItems ?? selections.size;
        if (result) {
            correctCount.textContent = result.currentScore;
        }
    }

    function startElapsedTimer() {
        const parsedStart = Date.parse(shell.dataset.startedAt);
        const startedAt = Number.isNaN(parsedStart) ? Date.now() : parsedStart;
        const renderElapsedTime = () => {
            const elapsedSeconds = Math.max(0, Math.floor((Date.now() - startedAt) / 1000));
            const hours = Math.floor(elapsedSeconds / 3600);
            const minutes = Math.floor((elapsedSeconds % 3600) / 60);
            const seconds = elapsedSeconds % 60;
            elapsedTime.textContent = [hours, minutes, seconds]
                .map(value => String(value).padStart(2, '0'))
                .join(':');
        };

        renderElapsedTime();
        window.setInterval(renderElapsedTime, 1000);
    }

    function showActiveItem() {
        items.forEach((item, index) => {
            item.classList.toggle('is-active', index === activeIndex);
        });
        const activeItem = items[activeIndex];
        if (!activeItem) {
            return;
        }
        currentItem.textContent = activeIndex + 1;
        currentTitle.textContent = `Prueba ${activeIndex + 1}`;
        const itemLabel = activeItem.querySelector('.item-title h3')?.textContent;
        setStatus(`${itemLabel}. Arrastra una opción al recuadro punteado.`);
    }

    function advanceAfterSave() {
        if (activeIndex < items.length - 1) {
            activeIndex += 1;
            showActiveItem();
            return;
        }
        finishButton.disabled = false;
        setStatus('Todas las respuestas fueron registradas. Puedes finalizar y calificar.');
    }

    async function finishTest() {
        if (selections.size < totalItems) {
            const missing = totalItems - selections.size;
            setStatus(`Faltan ${missing} respuestas antes de finalizar.`);
            return;
        }
        try {
            const result = await postResponses(`/api/attempts/${attemptId}/complete`, Array.from(selections.values()));
            window.location.assign(result.resultsUrl);
        } catch (error) {
            setStatus('No se pudo finalizar. Revisa la conexión e intenta de nuevo.');
        }
    }

    async function postResponses(url, responses) {
        setBusy(true);
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({responses})
            });
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            return await response.json();
        } finally {
            setBusy(false);
        }
    }

    function setBusy(nextBusy) {
        busy = nextBusy;
        shell.classList.toggle('is-busy', busy);
        finishButton.disabled = busy || selections.size < totalItems;
    }

    function setStatus(message) {
        statusMessage.textContent = message;
    }
})();
