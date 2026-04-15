import tkinter as tk
from tkinter import messagebox
from zeep import Client

# SOAP WSDL
USER_WSDL = "http://localhost:8080/UserService?wsdl"
ACCOUNT_WSDL = "http://localhost:8081/AccountService?wsdl"

user_client = Client(USER_WSDL)
account_client = Client(ACCOUNT_WSDL)


# =========================
# USER SERVICE
# =========================

def create_user():
    try:
        result = user_client.service.createUser(
            first_name_entry.get(),
            last_name_entry.get(),
            email_entry.get(),
            password_entry.get()
        )
        messagebox.showinfo("UserService", result)
    except Exception as e:
        messagebox.showerror("Błąd UserService", str(e))


def get_users():
    try:
        users = user_client.service.getAllUsers()

        output.delete("1.0", tk.END)
        output.insert(tk.END, "=== USERS ===\n")

        for u in users:
            output.insert(tk.END, f"{u}\n")

    except Exception as e:
        messagebox.showerror("Błąd UserService", str(e))


def delete_user():
    try:
        result = user_client.service.deleteUser(int(user_id_entry.get()))
        messagebox.showinfo("UserService", result)
    except Exception as e:
        messagebox.showerror("Błąd UserService", str(e))


# =========================
# ACCOUNT SERVICE
# =========================

def add_balance():
    try:
        result = account_client.service.addBalanceToUser(
            int(user_id_entry.get()),
            currency_entry.get(),
            amount_entry.get()
        )
        messagebox.showinfo("AccountService", result)
    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


def get_balances():
    try:
        balances = account_client.service.getBalancesForUser(
            int(user_id_entry.get())
        )

        output.delete("1.0", tk.END)
        output.insert(tk.END, "=== BALANCES ===\n")

        for b in balances:
            output.insert(tk.END, f"{b}\n")

    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


def get_balance_currency():
    try:
        result = account_client.service.getBalanceForUserAndCurrency(
            int(user_id_entry.get()),
            currency_entry.get()
        )
        messagebox.showinfo("Balance", str(result))
    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


# =========================
# UI
# =========================

root = tk.Tk()
root.title("SOAP Kantor System (User + Account)")
root.geometry("800x650")


# -------- USER SECTION --------
tk.Label(root, text="USER SERVICE", font=("Arial", 14)).pack()

first_name_entry = tk.Entry(root)
first_name_entry.insert(0, "Jan")
first_name_entry.pack()

last_name_entry = tk.Entry(root)
last_name_entry.insert(0, "Kowalski")
last_name_entry.pack()

email_entry = tk.Entry(root)
email_entry.insert(0, "test@test.com")
email_entry.pack()

password_entry = tk.Entry(root)
password_entry.insert(0, "1234")
password_entry.pack()

tk.Button(root, text="Create User", command=create_user).pack()
tk.Button(root, text="Get Users", command=get_users).pack()
tk.Button(root, text="Delete User", command=delete_user).pack()


# -------- ACCOUNT SECTION --------
tk.Label(root, text="ACCOUNT SERVICE", font=("Arial", 14)).pack()

user_id_entry = tk.Entry(root)
user_id_entry.insert(0, "1")
user_id_entry.pack()

currency_entry = tk.Entry(root)
currency_entry.insert(0, "PLN")
currency_entry.pack()

amount_entry = tk.Entry(root)
amount_entry.insert(0, "100")
amount_entry.pack()

tk.Button(root, text="Add Balance", command=add_balance).pack()
tk.Button(root, text="Get Balances", command=get_balances).pack()
tk.Button(root, text="Get Balance (Currency)", command=get_balance_currency).pack()


# -------- OUTPUT --------
output = tk.Text(root, height=20)
output.pack(fill="both", expand=True)


root.mainloop()