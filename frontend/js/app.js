const API_URL = "http://localhost:8081/api";

const state = {
    products: [],
    categories: [],
    editingProductId: null
};

const elements = {
    apiStatus: document.getElementById("api-status"),
    totalProducts: document.getElementById("total-products"),
    totalCategories: document.getElementById("total-categories"),
    averagePrice: document.getElementById("average-price"),

    productTable: document.getElementById("product-table"),
    productSearch: document.getElementById("product-search"),
    categoryFilter: document.getElementById("category-filter"),

    categoryForm: document.getElementById("category-form"),
    categoryName: document.getElementById("category-name"),
    categoryList: document.getElementById("category-list"),

    productDialog: document.getElementById("product-dialog"),
    productForm: document.getElementById("product-form"),
    dialogTitle: document.getElementById("dialog-title"),
    productName: document.getElementById("product-name"),
    productPrice: document.getElementById("product-price"),
    productCategory: document.getElementById("product-category"),

    saveProductButton: document.getElementById("save-product-button"),
    toast: document.getElementById("toast")
};

let toastTimeout;

// Comunicação HTTP com a API REST.
async function apiRequest(path, options = {}) {
    const { method = "GET", body } = options;

    const response = await fetch(`${API_URL}${path}`, {
        method,
        headers: body !== undefined
            ? { "Content-Type": "application/json" }
            : {},
        body: body !== undefined ? JSON.stringify(body) : undefined
    });

    const responseText = await response.text();

    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {
        let message = `Erro HTTP ${response.status}`;

        if (data && typeof data === "object") {
            if (data.mensagens) {
                message = Object.values(data.mensagens).join(" ");
            } else {
                message = data.mensagem || data.erro || message;
            }
        }

        throw new Error(message);
    }

    return data;
}

function showToast(message, isError = false) {
    clearTimeout(toastTimeout);

    elements.toast.textContent = message;
    elements.toast.classList.toggle("error", isError);
    elements.toast.classList.add("show");

    toastTimeout = setTimeout(() => {
        elements.toast.classList.remove("show");
    }, 3500);
}

function formatCurrency(value) {
    return new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    }).format(Number(value));
}

// Escapa os dados antes de inseri-los no HTML.
function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    })[char]);
}

async function loadData() {
    try {
        const categories = await apiRequest("/categorias");
        const products = await apiRequest("/produtos");

        state.categories = categories;
        state.products = products;

        elements.apiStatus.textContent = "Conectada";

        document.querySelector(".connection-indicator").style.background =
            "#45c88b";

        renderAll();

    } catch (error) {
        elements.apiStatus.textContent = "Desconectada";

        document.querySelector(".connection-indicator").style.background =
            "#e05d64";

        showToast(`Falha ao carregar dados: ${error.message}`, true);
    }
}

function renderAll() {
    renderStatistics();
    renderCategoryControls();
    renderCategories();
    renderProducts();
}

function renderStatistics() {
    elements.totalProducts.textContent = state.products.length;
    elements.totalCategories.textContent = state.categories.length;

    const average = state.products.length
        ? state.products.reduce((sum, product) =>
            sum + Number(product.preco), 0) / state.products.length
        : 0;

    elements.averagePrice.textContent = formatCurrency(average);
}

function renderCategoryControls() {
    const currentFilter = elements.categoryFilter.value;
    const currentCategory = elements.productCategory.value;

    elements.categoryFilter.innerHTML =
        '<option value="">Todas as categorias</option>' +
        state.categories.map(category => `
            <option value="${category.id}">
                ${escapeHtml(category.nome)}
            </option>
        `).join("");

    elements.productCategory.innerHTML =
        '<option value="">Selecione uma categoria</option>' +
        state.categories.map(category => `
            <option value="${category.id}">
                ${escapeHtml(category.nome)}
            </option>
        `).join("");

    if (state.categories.some(category =>
        String(category.id) === currentFilter)) {
        elements.categoryFilter.value = currentFilter;
    }

    if (state.categories.some(category =>
        String(category.id) === currentCategory)) {
        elements.productCategory.value = currentCategory;
    }
}

function renderCategories() {
    if (!state.categories.length) {
        elements.categoryList.innerHTML = `
            <p class="empty-state">
                Nenhuma categoria cadastrada.
            </p>
        `;
        return;
    }

    elements.categoryList.innerHTML = state.categories.map(category => `
        <div class="category-item">
            <strong>${escapeHtml(category.nome)}</strong>

            <button
                class="action-button delete"
                data-action="delete-category"
                data-id="${category.id}">
                Excluir
            </button>
        </div>
    `).join("");
}

function renderProducts() {
    const search = elements.productSearch.value.trim().toLowerCase();
    const selectedCategory = elements.categoryFilter.value;

    const filteredProducts = state.products.filter(product => {
        const matchesSearch = product.nome.toLowerCase().includes(search);

        const matchesCategory = !selectedCategory ||
            String(product.categoria.id) === selectedCategory;

        return matchesSearch && matchesCategory;
    });

    if (!filteredProducts.length) {
        elements.productTable.innerHTML = `
            <tr>
                <td colspan="4" class="empty-state">
                    Nenhum produto encontrado.
                </td>
            </tr>
        `;
        return;
    }

    elements.productTable.innerHTML = filteredProducts.map(product => `
        <tr>
            <td class="product-name">
                ${escapeHtml(product.nome)}
            </td>

            <td>
                <span class="category-tag">
                    ${escapeHtml(product.categoria.nome)}
                </span>
            </td>

            <td>${formatCurrency(product.preco)}</td>

            <td>
                <div class="actions">
                    <button
                        class="action-button"
                        data-action="edit-product"
                        data-id="${product.id}">
                        Editar
                    </button>

                    <button
                        class="action-button delete"
                        data-action="delete-product"
                        data-id="${product.id}">
                        Excluir
                    </button>
                </div>
            </td>
        </tr>
    `).join("");
}

function openProductDialog(product = null) {
    state.editingProductId = product ? product.id : null;

    elements.productForm.reset();

    renderCategoryControls();

    if (product) {
        elements.dialogTitle.textContent = "Editar produto";
        elements.saveProductButton.textContent = "Salvar alterações";

        elements.productName.value = product.nome;
        elements.productPrice.value = product.preco;
        elements.productCategory.value = product.categoria.id;
    } else {
        elements.dialogTitle.textContent = "Novo produto";
        elements.saveProductButton.textContent = "Salvar produto";
    }

    if (!state.categories.length) {
        showToast("Cadastre uma categoria antes de cadastrar produtos.", true);
    }

    elements.productDialog.showModal();
}

async function saveProduct(event) {
    event.preventDefault();

    const nome = elements.productName.value.trim();
    const preco = Number(elements.productPrice.value);
    const categoriaId = Number(elements.productCategory.value);

    if (!nome || !Number.isFinite(preco) || preco <= 0 ||
        !Number.isInteger(categoriaId) || categoriaId <= 0) {
        showToast("Preencha todos os campos corretamente.", true);
        return;
    }

    const body = {
        nome,
        preco,
        categoriaId
    };

    const isEditing = state.editingProductId !== null;

    const path = isEditing
        ? `/produtos/${state.editingProductId}`
        : "/produtos";

    const method = isEditing ? "PUT" : "POST";

    elements.saveProductButton.disabled = true;

    try {
        await apiRequest(path, { method, body });

        elements.productDialog.close();

        await loadData();

        showToast(isEditing
            ? "Produto atualizado com sucesso!"
            : "Produto cadastrado com sucesso!");

    } catch (error) {
        showToast(error.message, true);
    } finally {
        elements.saveProductButton.disabled = false;
    }
}

async function deleteProduct(id) {
    const product = state.products.find(item => item.id === id);

    if (!product || !confirm(
        `Deseja realmente excluir "${product.nome}"?`)) {
        return;
    }

    try {
        await apiRequest(`/produtos/${id}`, { method: "DELETE" });

        await loadData();

        showToast("Produto excluído com sucesso!");

    } catch (error) {
        showToast(error.message, true);
    }
}

async function saveCategory(event) {
    event.preventDefault();

    const nome = elements.categoryName.value.trim();

    if (!nome) {
        showToast("Informe o nome da categoria.", true);
        return;
    }

    try {
        await apiRequest("/categorias", {
            method: "POST",
            body: { nome }
        });

        elements.categoryForm.reset();

        await loadData();

        showToast("Categoria cadastrada com sucesso!");

    } catch (error) {
        showToast(error.message, true);
    }
}

async function deleteCategory(id) {
    const category = state.categories.find(item => item.id === id);

    if (!category || !confirm(
        `Deseja excluir a categoria "${category.nome}"?`)) {
        return;
    }

    try {
        await apiRequest(`/categorias/${id}`, { method: "DELETE" });

        await loadData();

        showToast("Categoria excluída com sucesso!");

    } catch (error) {
        showToast(error.message, true);
    }
}

// Eventos dos formulários e controles.
elements.categoryForm.addEventListener("submit", saveCategory);
elements.productForm.addEventListener("submit", saveProduct);

elements.productSearch.addEventListener("input", renderProducts);
elements.categoryFilter.addEventListener("change", renderProducts);

document.getElementById("new-product-button")
    .addEventListener("click", () => openProductDialog());

document.getElementById("new-product-button-secondary")
    .addEventListener("click", () => openProductDialog());

document.getElementById("close-dialog")
    .addEventListener("click", () => elements.productDialog.close());

document.getElementById("cancel-dialog")
    .addEventListener("click", () => elements.productDialog.close());

// Eventos delegados da tabela e da listagem de categorias.
elements.productTable.addEventListener("click", event => {
    const button = event.target.closest("button[data-action]");

    if (!button) return;

    const id = Number(button.dataset.id);
    const action = button.dataset.action;

    if (action === "edit-product") {
        const product = state.products.find(item => item.id === id);

        if (product) openProductDialog(product);
    }

    if (action === "delete-product") {
        deleteProduct(id);
    }
});

elements.categoryList.addEventListener("click", event => {
    const button = event.target.closest(
        'button[data-action="delete-category"]'
    );

    if (button) {
        deleteCategory(Number(button.dataset.id));
    }
});

// Carrega os dados assim que a interface é aberta.
loadData();
