# Paper Trade Plugin

> 🇺🇦 Репозиторій було перенесено на новий з деяких причин, подальші оновлення виходитимуть у цьому репозиторії.

> 🇬🇧 The repository has been moved to a new one for certain reasons, further updates will be released in this repository.

A Paper 26.1.2 plugin that allows players to safely trade items with each other at any distance using a GUI.

---

## Ukrainian

### Опис
Плагін для безпечного обміну предметами між гравцями на будь-якій відстані через зручний GUI інтерфейс.

### Як користуватись
1. Введи `/trade <нік гравця>` щоб надіслати запит на трейд
2. Інший гравець отримає повідомлення з кнопками **[Прийняти]** / **[Відхилити]**
3. При прийнятті — в обох відкривається вікно трейду:
   - **Рядки 1-2** — твої речі (поклади сюди що хочеш запропонувати)
   - **Рядок 3** — розділювач
   - **Рядки 4-5** — речі опонента (оновлюються в реальному часі)
   - **Рядок 6** — кнопки: скасувати | підтвердити
4. Коли обидва підтвердили — відкривається вікно **підтвердження** з фінальним переглядом
5. Обидва підтверджують — відбувається обмін

### Команди
| Команда | Опис |
|---------|------|
| `/trade <гравець>` | Надіслати запит на трейд |

### Особливості
- Речі опонента оновлюються live поки трейд активний
- Якщо партнер вже підтвердив і ти змінюєш свої речі — його підтвердження скидається
- При виході гравця або закритті вікна — трейд скасовується, речі повертаються
- Запит на трейд закінчується через 30 секунд

---

## English

### Description
A plugin for safe item trading between players at any distance through a clean GUI interface.

### How to use
1. Type `/trade <player name>` to send a trade request
2. The other player receives a chat message with **[Accept]** / **[Decline]** buttons
3. On accept — both players get a trade window:
   - **Rows 1-2** — your items (place what you want to offer here)
   - **Row 3** — divider
   - **Rows 4-5** — opponent's items (updates in real time)
   - **Row 6** — buttons: cancel | confirm
4. When both players confirm — a **preview window** opens for final review
5. Both confirm again — items are exchanged

### Commands
| Command | Description |
|---------|-------------|
| `/trade <player>` | Send a trade request |

### Features
- Opponent's items update live while the trade is active
- If a partner already confirmed and you change your items — their confirmation resets
- If a player disconnects or closes the window — trade is cancelled and items are returned
- Trade requests expire after 30 seconds

---

## Requirements
- Paper 26.1.2+
- Java 25+

## Author
**s1sp (s1si4kaa)** — [Portfolio](https://s1spa.github.io/Portfolio/)