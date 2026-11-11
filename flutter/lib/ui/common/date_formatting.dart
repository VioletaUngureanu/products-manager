import '../../domain/cosmetic_product.dart';

/// Small pure helpers for turning dates and expiry info into display text.
/// Kept together so formatting is consistent across the list, detail and form.

/// Month names, indexed 1..12. A lookup table instead of pulling in the
/// `intl` package keeps the dependency list minimal (assignment rule: minimal
/// changes, no speculative extras).
const List<String> _monthNames = [
  '', // index 0 unused so month 1 == January
  'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
  'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
];

/// Formats a date as e.g. "7 Nov 2026", or a dash when the date is null.
String formatDateOrDash(DateTime? date) {
  if (date == null) return '—';
  return '${date.day} ${_monthNames[date.month]} ${date.year}';
}

/// A short, human-friendly expiry status line for a product, matching the
/// wording used on the list card:
///  * unopened -> "Shelf life: N months"
///  * expired  -> "Expired N days/months ago"
///  * opened   -> "Expires in N days/months"
String expiryStatusText(CosmeticProduct product) {
  final days = product.daysUntilExpiry;
  if (days == null) {
    return 'Shelf life: ${product.shelfLifeMonths} months';
  }
  if (days < 0) {
    return 'Expired ${_humanDuration(-days)} ago';
  }
  if (days == 0) {
    return 'Expires today';
  }
  return 'Expires in ${_humanDuration(days)}';
}

/// Number of days above which we show the duration in months instead of days.
const int _daysToSwitchToMonths = 45;

/// Approximate number of days in a month, used only for a rough "N months"
/// display label (not for real date maths).
const int _approxDaysPerMonth = 30;

/// Turns a day count into "N day(s)" or, for larger values, "N month(s)".
String _humanDuration(int days) {
  if (days < _daysToSwitchToMonths) {
    return days == 1 ? '1 day' : '$days days';
  }
  final months = (days / _approxDaysPerMonth).round();
  return months == 1 ? '1 month' : '$months months';
}
