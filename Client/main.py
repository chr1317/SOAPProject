import tkinter as tk
from tkinter import messagebox, ttk, filedialog
from zeep import Client, xsd, Transport
import requests
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
from PIL import Image, ImageTk
import io
import os
import logging

# =========================
# CONFIG - IP SERWERA
# =========================

SERVER_IP = None

def ask_server_ip():
    global SERVER_IP

    win = tk.Tk()
    win.title("Konfiguracja serwera")
    win.geometry("350x150")

    tk.Label(win, text="Podaj IP serwera SOAP:", font=("Arial", 11)).pack(pady=10)

    entry = tk.Entry(win, width=30)
    entry.insert(0, "localhost")  # domyślnie
    entry.pack(pady=5)

    def start():
        global SERVER_IP
        SERVER_IP = entry.get().strip()
        win.destroy()

    tk.Button(win, text="Start", command=start).pack(pady=10)

    win.mainloop()

ask_server_ip()

# =========================
# LOGGING / MONITORING
# =========================
logging.basicConfig(
    level=logging.DEBUG,
    format="%(asctime)s | %(levelname)s | %(message)s"
)

logging.getLogger("zeep").setLevel(logging.DEBUG)

logging.info("Aplikacja startuje...")

# =========================
# SOAP
# =========================
USER_WSDL = f"https://{SERVER_IP}/UserService?wsdl"
ACCOUNT_WSDL = f"https://{SERVER_IP}/AccountService?wsdl"

logging.info("Łączenie z SOAP services...")

session = requests.Session()
session.verify = False  # WYŁĄCZA weryfikację SSL

transport = Transport(session=session)

user_client = Client(USER_WSDL, transport=transport)
account_client = Client(ACCOUNT_WSDL, transport=transport)

edit_widgets = {}
editing_item = None

logged_user_id = None


# =========================
# UI STYLE HELPERS
# =========================
def section(parent, title):
    logging.debug(f"Tworzenie sekcji UI: {title}")
    frame = tk.LabelFrame(parent, text=title, padx=15, pady=15, font=("Arial", 11, "bold"))
    frame.pack(fill="x", padx=15, pady=10)
    return frame


def add_placeholder(entry, text):
    entry.insert(0, text)
    entry.config(fg="grey")

    def in_f(e):
        if entry.get() == text:
            entry.delete(0, tk.END)
            entry.config(fg="black")

    def out_f(e):
        if not entry.get():
            entry.insert(0, text)
            entry.config(fg="grey")

    entry.bind("<FocusIn>", in_f)
    entry.bind("<FocusOut>", out_f)


# =========================
# WALUTY
# =========================
currency_display = {
    "PLN": "🇵🇱 PLN - Polski złoty",
    "EUR": "🇪🇺 EUR - Euro",
    "USD": "🇺🇸 USD - Dolar",
    "GBP": "🇬🇧 GBP - Funt"
}

display_to_code = {v: k for k, v in currency_display.items()}

def get_code(v):
    return display_to_code.get(v, v)


# =========================
# LOGIN
# =========================
def login():
    global logged_user_id

    try:
        ok = user_client.service.authenticateUser(
            login_email.get(),
            login_password.get()
        )

        if not ok:
            messagebox.showerror("Logowanie", "Błędne dane")
            return

        logged_user_id = int(user_client.service.getUserIdByEmail(login_email.get()))

        # 👉 UPDATE UI
        user_label.config(text=f"Użytkownik: {login_email.get()}")
        logout_btn.pack(side="right", padx=10)

        notebook.select(tab_wallet)  # opcjonalnie przełącz zakładkę

        messagebox.showinfo("OK", f"Zalogowano ID={logged_user_id}")
        load_user_currencies()

    except Exception as e:
        messagebox.showerror("Błąd", str(e))

def logout():
    global logged_user_id

    logging.info("Wylogowanie")

    logged_user_id = None

    user_label.config(text="Niezalogowany")
    logout_btn.pack_forget()

    login_email.delete(0, tk.END)
    login_password.delete(0, tk.END)

    notebook.select(tab_login)

    messagebox.showinfo("Wylogowano", "Zostałeś wylogowany")

def require_login():
    if logged_user_id is None:
        logging.warning("Brak logowania - blokada akcji")
        messagebox.showerror("Błąd", "Zaloguj się")
        return False
    return True


# =========================
# WALUTY
# =========================
def load_currencies():
    logging.info("Ładowanie walut...")
    currencies = list(account_client.service.getAvailableCurrencyCodes())
    display = [currency_display.get(c, c) for c in currencies]

    add_currency['values'] = display
    to_currency['values'] = display

    logging.debug(f"Dostępne waluty: {display}")


def load_user_currencies():
    if not require_login():
        return

    logging.info(f"Ładowanie walut user_id={logged_user_id}")

    currencies = list(account_client.service.getUserCurrencies(logged_user_id))
    display = [currency_display.get(c, c) for c in currencies]

    from_currency['values'] = display
    if display:
        from_currency.set(display[0])

    logging.debug(f"Waluty usera: {display}")


# =========================
# USER
# =========================

def has_avatar(user_id):
    try:
        data = user_client.service.getAvatar(int(user_id))
        return data is not None
    except:
        return False
    
def show_avatar(user_id):
    try:
        data_handler = user_client.service.getAvatar(int(user_id))

        if not data_handler:
            messagebox.showinfo("Avatar", "Brak avatara")
            return

        image_data = data_handler

        image = Image.open(io.BytesIO(image_data))
        image = image.resize((200, 200))

        img = ImageTk.PhotoImage(image)

        win = tk.Toplevel(root)
        win.title(f"Avatar user {user_id}")

        label = tk.Label(win, image=img)
        label.image = img
        label.pack()

    except Exception as e:
        messagebox.showerror("Błąd avatara", str(e))

def parse_user(text):
    try:
        # zabezpieczenie gdy przyjdzie XML node albo string
        text = str(text)

        # ID
        id_ = text.split("ID=")[1].split(",")[0].strip()

        # email
        email = text.split("email=")[1].split("First name=")[0].strip()

        # first name
        first = text.split("First name=")[1].split("Last Name=")[0].strip()

        # last name
        last = text.split("Last Name=")[1].split("avatar=")[0].strip()

        return id_, first, last, email

    except Exception as e:
        logging.error(f"Parse error: {e} | text={text}")
        return "", "", "", ""

def create_user():
    logging.info("Tworzenie użytkownika")

    try:
        res = user_client.service.createUser(
            first_name.get(),
            last_name.get(),
            email_entry.get(),
            password_entry.get()
        )

        logging.debug(f"createUser response: {res}")
        messagebox.showinfo("User", res)

    except Exception as e:
        logging.exception("Błąd create_user")
        messagebox.showerror("Błąd", str(e))


def get_users():
    try:
        users = user_client.service.getAllUsers()
        print("DEBUG USERS:", users)

        for row in user_table.get_children():
            user_table.delete(row)

        if not users:
            return

        for u in users:
            if not u:
                continue

            id_, first, last, email = parse_user(u)

            avatar_icon = "🔍" if has_avatar(id_) else "❌"

            user_table.insert("", "end", values=(
                id_,
                first,
                last,
                email,
                avatar_icon,
                "✏️",
                "🗑️",
                "📁"
            ))

    except Exception as e:
        logging.exception("get_users error")
        messagebox.showerror("Błąd", str(e))

def on_table_click(event):
    item = user_table.identify_row(event.y)
    column = user_table.identify_column(event.x)

    if not item:
        return

    values = user_table.item(item, "values")
    user_id = values[0]

    if column == "#5":
        show_avatar(user_id)
    elif column == "#6":  # edit
        start_inline_edit(item)

    elif column == "#7":  # delete
        delete_user(user_id)

    elif column == "#8":  # avatar
        upload_avatar_ui(user_id)

def edit_user(item):
    values = user_table.item(item, "values")

    edit_window = tk.Toplevel(root)
    edit_window.title("Edycja użytkownika")

    id_, fn, ln, email, *_ = values

    e_fn = tk.Entry(edit_window)
    e_fn.insert(0, fn)
    e_fn.pack()

    e_ln = tk.Entry(edit_window)
    e_ln.insert(0, ln)
    e_ln.pack()

    e_email = tk.Entry(edit_window)
    e_email.insert(0, email)
    e_email.pack()

    def save():
        try:
            res = user_client.service.updateUser(
                int(id_),
                e_fn.get(),
                e_ln.get(),
                e_email.get()
            )
            messagebox.showinfo("OK", res)
            edit_window.destroy()
            get_users()
        except Exception as e:
            messagebox.showerror("Błąd", str(e))

    tk.Button(edit_window, text="✔ Zapisz", command=save).pack()

def delete_user(user_id):
    if not messagebox.askyesno("Potwierdzenie", "Usunąć użytkownika?"):
        return

    try:
        res = user_client.service.deleteUser(int(user_id))
        messagebox.showinfo("OK", res)
        get_users()
    except Exception as e:
        messagebox.showerror("Błąd", str(e))

def upload_avatar_ui(user_id):
    file_path = filedialog.askopenfilename(
        filetypes=[("Images", "*.jpg *.jpeg *.png")]
    )

    if not file_path:
        return

    try:
        with open(file_path, "rb") as f:
            data = f.read()

        res = user_client.service.uploadAvatar(int(user_id), data)

        messagebox.showinfo("OK", res)
        get_users()
        user_table.update()

    except Exception as e:
        messagebox.showerror("Błąd", str(e))


def start_inline_edit(item):
    global edit_widgets, editing_item

    cancel_inline_edit()

    editing_item = item
    values = user_table.item(item, "values")

    cols = ["id", "firstName", "lastName", "email"]

    for i, col in enumerate(cols):
        bbox = user_table.bbox(item, f"#{i+1}")
        if not bbox:
            continue

        x, y, w, h = bbox

        entry = tk.Entry(user_table)
        entry.insert(0, values[i])
        entry.place(x=x, y=y, width=w, height=h)

        edit_widgets[col] = entry

    # =========================
    # PRZYCISKI
    # =========================

    x_save, y_save, w, h = user_table.bbox(item, "#5")

    save_btn = tk.Button(user_table, text="✔", command=save_inline_edit)
    save_btn.place(x=x_save, y=y_save, width=35, height=h)

    cancel_btn = tk.Button(user_table, text="✖", command=cancel_inline_edit)
    cancel_btn.place(x=x_save + 40, y=y_save, width=35, height=h)

    edit_widgets["save_btn"] = save_btn
    edit_widgets["cancel_btn"] = cancel_btn

def save_inline_edit():
    global editing_item

    try:
        id_ = edit_widgets["id"].get()
        fn = edit_widgets["firstName"].get()
        ln = edit_widgets["lastName"].get()
        email = edit_widgets["email"].get()

        res = user_client.service.updateUser(
            int(id_),
            fn,
            ln,
            email
        )

        messagebox.showinfo("OK", res)

        cancel_inline_edit()
        get_users()

    except Exception as e:
        messagebox.showerror("Błąd", str(e))

def cancel_inline_edit():
    global edit_widgets, editing_item

    for w in edit_widgets.values():
        try:
            w.destroy()
        except:
            pass

    edit_widgets = {}
    editing_item = None

# =========================
# PORTFEL
# =========================
def deposit():
    if not require_login():
        return

    logging.info("DEPÓZYT")

    try:
        account_client.service.addBalanceToUser(
            logged_user_id,
            get_code(add_currency.get()),
            amount.get()
        )

        logging.debug("Wpłata zakończona")
        load_user_currencies()
        messagebox.showinfo("Sukces", "Wpłata zakończona pomyślnie")

    except Exception as e:
        logging.exception("Błąd deposit")
        messagebox.showerror("Błąd wpłaty", str(e))


def withdraw():
    if not require_login():
        return

    logging.info("WYPŁATA")

    try:
        account_client.service.withdrawBalance(
            logged_user_id,
            get_code(add_currency.get()),
            amount.get()
        )

        logging.debug("Wypłata zakończona")
        load_user_currencies()
        messagebox.showinfo("Sukces", "Wypłata zakończona pomyślnie")

    except Exception as e:
        logging.exception("Błąd withdraw")
        messagebox.showerror("Błąd wypłaty", str(e))


def show_balances():
    if not require_login():
        return

    logging.info("Pobieranie sald")

    try:
        balances = account_client.service.getBalancesForUser(logged_user_id)
        output.delete("1.0", tk.END)

        output.insert(tk.END, "=== PORTFEL ===\n\n")

        for b in balances:
            output.insert(tk.END, f"{b}\n")

        logging.debug(f"Sald: {len(balances)}")

        messagebox.showinfo("Portfel", "Pobrano dane portfela")

    except Exception as e:
        logging.exception("Błąd show_balances")
        messagebox.showerror("Błąd portfela", str(e))


# =========================
# PDF
# =========================
def export_pdf():
    if not require_login():
        return

    logging.info("EXPORT PDF PORTFEL")

    try:
        balances = account_client.service.getBalancesForUser(logged_user_id)

        file = "portfel.pdf"
        doc = SimpleDocTemplate(file)
        styles = getSampleStyleSheet()

        elements = [Paragraph("PORTFEL", styles["Title"]), Spacer(1, 10)]
        for b in balances:
            elements.append(Paragraph(str(b), styles["Normal"]))

        doc.build(elements)

        logging.info("PDF zapisany: portfel.pdf")

        messagebox.showinfo("PDF", "Portfel zapisany jako portfel.pdf")
        os.startfile(file)

    except Exception as e:
        logging.exception("Błąd export_pdf")
        messagebox.showerror("Błąd PDF", str(e))


def export_history_pdf():
    if not require_login():
        return

    logging.info("EXPORT PDF HISTORIA")

    try:
        history = account_client.service.getAccountTransactionsForUser(logged_user_id)

        file = "historia.pdf"
        doc = SimpleDocTemplate(file)
        styles = getSampleStyleSheet()

        elements = [Paragraph("HISTORIA TRANSAKCJI", styles["Title"]), Spacer(1, 10)]
        for h in history:
            elements.append(Paragraph(str(h), styles["Normal"]))

        doc.build(elements)

        logging.info("PDF historia zapisany")

        messagebox.showinfo("PDF", "Historia zapisana jako historia.pdf")
        os.startfile(file)

    except Exception as e:
        logging.exception("Błąd export_history_pdf")
        messagebox.showerror("Błąd PDF", str(e))


# =========================
# KANTOR
# =========================
def exchange():
    if not require_login():
        return

    logging.info("EXCHANGE WALUT")

    try:
        account_client.service.exchangeCurrency(
            logged_user_id,
            get_code(from_currency.get()),
            get_code(to_currency.get()),
            exchange_amount.get()
        )

        logging.debug("Wymiana zakończona")

        load_user_currencies()
        messagebox.showinfo("Kantor", "Wymiana walut zakończona pomyślnie")

    except Exception as e:
        logging.exception("Błąd exchange")
        messagebox.showerror("Błąd kantoru", str(e))


def history():
    logging.info("HISTORIA TRANSAKCJI")

    try:
        data = account_client.service.getAccountTransactionsForUser(logged_user_id)
        output.delete("1.0", tk.END)

        output.insert(tk.END, "=== HISTORIA ===\n\n")

        for h in data:
            output.insert(tk.END, f"{h}\n")

        logging.debug(f"Transakcji: {len(data)}")

        messagebox.showinfo("Historia", "Pobrano historię transakcji")

    except Exception as e:
        logging.exception("Błąd history")
        messagebox.showerror("Błąd historii", str(e))


# =========================
# UI
# =========================
root = tk.Tk()
root.title("💰 System Kantorowy PRO")
root.geometry("1100x800")
root.configure(bg="#f2f2f2")

user_bar = tk.Frame(root, bg="#dddddd", height=40)
user_bar.pack(fill="x")

user_label = tk.Label(user_bar, text="Niezalogowany", bg="#dddddd", font=("Arial", 10))
user_label.pack(side="left", padx=10)

logout_btn = tk.Button(user_bar, text="Wyloguj", command=lambda: logout())
logout_btn.pack(side="right", padx=10)

logout_btn.pack_forget()

notebook = ttk.Notebook(root)
notebook.pack(fill="both", expand=True, padx=10, pady=10)

# LOGIN
tab_login = tk.Frame(notebook, bg="white")
notebook.add(tab_login, text="🔐 Logowanie")

login_box = section(tab_login, "Logowanie")

login_email = tk.Entry(login_box, width=40)
login_email.pack(pady=5)
add_placeholder(login_email, "Email")

login_password = tk.Entry(login_box, width=40, show="*")
login_password.pack(pady=5)
add_placeholder(login_password, "Hasło")

tk.Button(login_box, text="Zaloguj", width=25, command=login).pack(pady=10)

# USER
tab_user = tk.Frame(notebook, bg="white")
notebook.add(tab_user, text="👤 Użytkownicy")

# --- REJESTRACJA ---
user_box = section(tab_user, "Rejestracja")

first_name = tk.Entry(user_box, width=40)
first_name.pack()
add_placeholder(first_name, "Imię")

last_name = tk.Entry(user_box, width=40)
last_name.pack()
add_placeholder(last_name, "Nazwisko")

email_entry = tk.Entry(user_box, width=40)
email_entry.pack()
add_placeholder(email_entry, "Email")

password_entry = tk.Entry(user_box, width=40)
password_entry.pack()
add_placeholder(password_entry, "Hasło")

tk.Button(user_box, text="Dodaj użytkownika", width=25, command=create_user).pack(pady=5)
tk.Button(user_box, text="Pokaż użytkowników", width=25, command=get_users).pack(pady=5)

# --- TABELA USERS ---
table_frame = section(tab_user, "Lista użytkowników")

columns = ("id", "firstName", "lastName", "email", "avatar", "edit", "delete", "upload")

user_table = ttk.Treeview(table_frame, columns=columns, show="headings", height=10)

# nagłówki
user_table.heading("id", text="ID")
user_table.heading("firstName", text="Imię")
user_table.heading("lastName", text="Nazwisko")
user_table.heading("email", text="Email")
user_table.heading("avatar", text="Avatar")
user_table.heading("edit", text="Edycja")
user_table.heading("delete", text="Usuń")
user_table.heading("upload", text="Zmień awatar")

# szerokości
user_table.column("id", width=50)
user_table.column("firstName", width=120)
user_table.column("lastName", width=120)
user_table.column("email", width=200)
user_table.column("avatar", width=80)
user_table.column("edit", width=50)
user_table.column("delete", width=50)
user_table.column("upload", width=50)

user_table.pack(fill="both", expand=True)

# scroll (ważne przy większej liczbie userów)
scrollbar = ttk.Scrollbar(table_frame, orient="vertical", command=user_table.yview)
user_table.configure(yscroll=scrollbar.set)
scrollbar.pack(side="right", fill="y")

# event kliknięcia
user_table.bind("<Button-1>", on_table_click)

# PORTFEL
tab_wallet = tk.Frame(notebook, bg="white")
notebook.add(tab_wallet, text="💼 Portfel")

wallet_box = section(tab_wallet, "Portfel")

add_currency = ttk.Combobox(wallet_box, width=40)
add_currency.pack(pady=5)

amount = tk.Entry(wallet_box, width=40)
amount.pack()
add_placeholder(amount, "Kwota")

tk.Button(wallet_box, text="Wpłać", width=20, command=deposit).pack(pady=3)
tk.Button(wallet_box, text="Wypłać", width=20, command=withdraw).pack(pady=3)
tk.Button(wallet_box, text="Saldo", width=20, command=show_balances).pack(pady=3)
tk.Button(wallet_box, text="PDF portfel", width=20, command=export_pdf).pack(pady=5)

# KANTOR
tab_exchange = tk.Frame(notebook, bg="white")
notebook.add(tab_exchange, text="💱 Kantor")

exchange_box = section(tab_exchange, "Wymiana walut")

from_currency = ttk.Combobox(exchange_box, width=40)
from_currency.pack(pady=5)

to_currency = ttk.Combobox(exchange_box, width=40)
to_currency.pack(pady=5)

exchange_amount = tk.Entry(exchange_box, width=40)
exchange_amount.pack()
add_placeholder(exchange_amount, "Kwota")

tk.Button(exchange_box, text="Wymień", width=25, command=exchange).pack(pady=5)
tk.Button(exchange_box, text="Historia", width=25, command=history).pack(pady=5)
tk.Button(exchange_box, text="PDF historia", width=25, command=export_history_pdf).pack(pady=5)

# OUTPUT
output = tk.Text(root, height=12, font=("Consolas", 10))
output.pack(fill="both", expand=True, padx=10, pady=10)

load_currencies()

logging.info("Aplikacja uruchomiona")
root.mainloop()