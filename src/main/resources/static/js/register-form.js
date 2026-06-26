(function () {
    const municipalitiesByDepartment = {
        "Boaco": ["Boaco", "Camoapa", "San José de los Remates", "San Lorenzo", "Santa Lucía", "Teustepe"],
        "Carazo": ["Diriamba", "Dolores", "El Rosario", "Jinotepe", "La Conquista", "La Paz de Carazo", "San Marcos", "Santa Teresa"],
        "Chinandega": ["Chichigalpa", "Chinandega", "Cinco Pinos", "Corinto", "El Realejo", "El Viejo", "Posoltega", "Puerto Morazán", "San Francisco del Norte", "San Pedro del Norte", "Santo Tomás del Norte", "Somotillo", "Villanueva"],
        "Chontales": ["Acoyapa", "Comalapa", "El Coral", "Juigalpa", "La Libertad", "San Francisco de Cuapa", "San Pedro de Lóvago", "Santo Domingo", "Santo Tomás", "Villa Sandino"],
        "Estelí": ["Condega", "Estelí", "La Trinidad", "Pueblo Nuevo", "San Juan de Limay", "San Nicolás"],
        "Granada": ["Diriá", "Diriomo", "Granada", "Nandaime"],
        "Jinotega": ["El Cuá", "Jinotega", "La Concordia", "San José de Bocay", "San Rafael del Norte", "San Sebastián de Yalí", "Santa María de Pantasma", "Wiwilí de Jinotega"],
        "León": ["Achuapa", "El Jicaral", "El Sauce", "La Paz Centro", "Larreynaga", "León", "Nagarote", "Quezalguaque", "Santa Rosa del Peñón", "Telica"],
        "Madriz": ["Las Sabanas", "Palacagüina", "San José de Cusmapa", "San Juan de Río Coco", "San Lucas", "Somoto", "Telpaneca", "Totogalpa", "Yalagüina"],
        "Managua": ["Ciudad Sandino", "El Crucero", "Managua", "Mateare", "San Francisco Libre", "San Rafael del Sur", "Ticuantepe", "Tipitapa", "Villa El Carmen"],
        "Masaya": ["Catarina", "La Concepción", "Masatepe", "Masaya", "Nandasmo", "Nindirí", "Niquinohomo", "San Juan de Oriente", "Tisma"],
        "Matagalpa": ["Ciudad Darío", "Esquipulas", "Matagalpa", "Matiguás", "Muy Muy", "Rancho Grande", "Río Blanco", "San Dionisio", "San Isidro", "San Ramón", "Sébaco", "Terrabona", "El Tuma-La Dalia"],
        "Nueva Segovia": ["Ciudad Antigua", "Dipilto", "El Jícaro", "Jalapa", "Macuelizo", "Mozonte", "Murra", "Ocotal", "Quilalí", "San Fernando", "Santa María", "Wiwilí de Nueva Segovia"],
        "Rivas": ["Altagracia", "Belén", "Buenos Aires", "Cárdenas", "Moyogalpa", "Potosí", "Rivas", "San Jorge", "San Juan del Sur", "Tola"],
        "Río San Juan": ["El Almendro", "El Castillo", "Morrito", "San Carlos", "San Juan de Nicaragua", "San Miguelito"],
        "Región Autónoma de la Costa Caribe Norte": ["Bonanza", "Mulukukú", "Prinzapolka", "Puerto Cabezas", "Rosita", "Siuna", "Waslala", "Waspán"],
        "Región Autónoma de la Costa Caribe Sur": ["Bluefields", "Corn Island", "Desembocadura de Río Grande", "El Ayote", "El Rama", "El Tortuguero", "Kukra Hill", "La Cruz de Río Grande", "Laguna de Perlas", "Muelle de los Bueyes", "Nueva Guinea", "Paiwas"]
    };

    const months = [
        ["01", "Enero"],
        ["02", "Febrero"],
        ["03", "Marzo"],
        ["04", "Abril"],
        ["05", "Mayo"],
        ["06", "Junio"],
        ["07", "Julio"],
        ["08", "Agosto"],
        ["09", "Septiembre"],
        ["10", "Octubre"],
        ["11", "Noviembre"],
        ["12", "Diciembre"]
    ];

    const department = document.getElementById("department");
    const municipality = document.getElementById("municipality");
    const municipalityOptions = document.getElementById("municipality-options");
    const birthDate = document.getElementById("birthDate");
    const birthDay = document.getElementById("birthDay");
    const birthMonth = document.getElementById("birthMonth");
    const birthYear = document.getElementById("birthYear");
    const form = document.querySelector(".registration-form");

    if (!department || !municipality || !municipalityOptions || !birthDate || !birthDay || !birthMonth || !birthYear || !form) {
        return;
    }

    function addOption(select, value, label) {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = label;
        select.appendChild(option);
    }

    function allMunicipalities() {
        return [...new Set(Object.values(municipalitiesByDepartment).flat())].sort((a, b) => a.localeCompare(b, "es"));
    }

    function municipalitySuggestions() {
        const selectedDepartment = department.value;
        const departmentMunicipalities = municipalitiesByDepartment[selectedDepartment] || [];

        if (!departmentMunicipalities.length) {
            return allMunicipalities();
        }

        return [...new Set([...departmentMunicipalities, ...allMunicipalities()])];
    }

    function populateMunicipalities() {
        municipalityOptions.innerHTML = "";
        municipalitySuggestions().forEach((name) => {
            addOption(municipalityOptions, name, name);
        });
    }

    function populateYears() {
        const currentYear = new Date().getFullYear();
        for (let year = currentYear; year >= 1900; year -= 1) {
            addOption(birthYear, String(year), String(year));
        }
    }

    function populateMonths() {
        months.forEach(([value, label]) => addOption(birthMonth, value, label));
    }

    function daysInMonth(year, month) {
        if (!year || !month) {
            return 31;
        }
        return new Date(Number(year), Number(month), 0).getDate();
    }

    function populateDays(selectedDay) {
        const maxDay = daysInMonth(birthYear.value, birthMonth.value);
        birthDay.innerHTML = "";
        addOption(birthDay, "", "Día");

        for (let day = 1; day <= maxDay; day += 1) {
            const value = String(day).padStart(2, "0");
            addOption(birthDay, value, String(day));
        }

        if (selectedDay && Number(selectedDay) <= maxDay) {
            birthDay.value = selectedDay;
        }
    }

    function syncBirthDate() {
        if (birthYear.value && birthMonth.value && birthDay.value) {
            birthDate.value = `${birthYear.value}-${birthMonth.value}-${birthDay.value}`;
            birthDay.setCustomValidity("");
            birthMonth.setCustomValidity("");
            birthYear.setCustomValidity("");
            return true;
        }

        birthDate.value = "";
        return false;
    }

    function restoreBirthDate() {
        const value = birthDate.value;
        if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
            return;
        }

        const [year, month, day] = value.split("-");
        birthYear.value = year;
        birthMonth.value = month;
        populateDays(day);
        syncBirthDate();
    }

    department.addEventListener("change", populateMunicipalities);
    birthMonth.addEventListener("change", () => {
        const selectedDay = birthDay.value;
        populateDays(selectedDay);
        syncBirthDate();
    });
    birthYear.addEventListener("change", () => {
        const selectedDay = birthDay.value;
        populateDays(selectedDay);
        syncBirthDate();
    });
    birthDay.addEventListener("change", syncBirthDate);

    form.addEventListener("submit", (event) => {
        if (!syncBirthDate()) {
            birthDay.setCustomValidity("Selecciona día, mes y año.");
            birthDay.reportValidity();
            event.preventDefault();
        }
    });

    populateMunicipalities();
    populateMonths();
    populateYears();
    populateDays("");
    restoreBirthDate();
})();
