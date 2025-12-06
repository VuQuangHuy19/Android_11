"""Mô hình AI dự đoán chi tiêu"""
import numpy as np
from typing import List, Dict
from datetime import datetime
from collections import defaultdict
def predict_expense_ai(expenses: List[float]) -> float:
    if not expenses:
        return 0
    if len(expenses) == 1:
        return expenses[0]
    expenses_array = np.array(expenses)
    n = len(expenses_array)
    # Phương pháp 1: Linear Regression (dự đoán xu hướng)
    x = np.arange(n)
    # Tính hệ số hồi quy tuyến tính: y = ax + b
    if n >= 2:
        # Sử dụng công thức least squares
        x_mean = np.mean(x)
        y_mean = np.mean(expenses_array)
        numerator = np.sum((x - x_mean) * (expenses_array - y_mean))
        denominator = np.sum((x - x_mean) ** 2)
        if denominator != 0:
            slope = numerator / denominator
            intercept = y_mean - slope * x_mean
            # Dự đoán tháng tiếp theo (x = n)
            linear_prediction = slope * n + intercept
        else:
            linear_prediction = expenses_array[-1]
    else:
        linear_prediction = expenses_array[-1]
    alpha = 0.7  # Trọng số cho giá trị gần nhất
    beta = 0.3   # Trọng số cho trend
    if n >= 2:
        # Khởi tạo
        level = expenses_array[0]
        trend = expenses_array[1] - expenses_array[0] if n >= 2 else 0
        for i in range(1, n):
            prev_level = level
            level = alpha * expenses_array[i] + (1 - alpha) * (level + trend)
            trend = beta * (level - prev_level) + (1 - beta) * trend
        exp_smoothing_prediction = level + trend
    else:
        exp_smoothing_prediction = expenses_array[-1]
    
    # Phương pháp 3: Weighted Moving Average với trọng số exponential
    # Tháng gần nhất có trọng số cao hơn
    weights = np.exp(np.linspace(-2, 0, n))  # Exponential weights
    weights = weights / np.sum(weights)  # Normalize
    weighted_avg = np.sum(expenses_array * weights)
    
    # Phương pháp 4: Momentum và Volatility Adjustment
    if n >= 3:
        # Tính momentum (tốc độ thay đổi)
        recent_change = expenses_array[-1] - expenses_array[-2]
        prev_change = expenses_array[-2] - expenses_array[-3] if n >= 3 else recent_change
        momentum = (recent_change + prev_change) / 2
        
        # Tính volatility (độ biến động)
        if n >= 3:
            volatility = np.std(expenses_array[-min(3, n):])
            mean_recent = np.mean(expenses_array[-min(3, n):])
            # Điều chỉnh dựa trên volatility (nếu biến động lớn, dự đoán thận trọng hơn)
            volatility_factor = 1 - min(0.3, volatility / (mean_recent + 1e-6))
        else:
            momentum = recent_change
            volatility_factor = 1.0
    else:
        momentum = expenses_array[-1] - expenses_array[-2] if n >= 2 else 0
        volatility_factor = 1.0
    
    momentum_prediction = expenses_array[-1] + momentum * volatility_factor
    
    # Phương pháp 5: Seasonality (tính theo mùa trong năm)
    # Nếu có đủ dữ liệu (ít nhất 12 tháng), tính seasonality
    seasonality_factor = 1.0
    if n >= 12:
        # Tính trung bình theo tháng trong năm (nếu có dữ liệu nhiều năm)
        # Đơn giản hóa: so sánh với cùng tháng trước đó
        if n >= 12:
            # Lấy giá trị cùng tháng năm trước (nếu có)
            seasonal_index = n % 12
            if seasonal_index == 0:
                seasonal_index = 12
            # Tìm các tháng tương ứng trong lịch sử
            seasonal_values = []
            for i in range(seasonal_index - 1, n, 12):
                if i < n:
                    seasonal_values.append(expenses_array[i])
            if len(seasonal_values) >= 2:
                seasonal_avg = np.mean(seasonal_values[:-1])  # Loại bỏ tháng hiện tại
                current_seasonal = seasonal_values[-1] if seasonal_values else expenses_array[-1]
                if seasonal_avg > 0:
                    seasonality_factor = current_seasonal / seasonal_avg
    
    # Ensemble: Kết hợp các phương pháp với trọng số
    # Trọng số dựa trên độ tin cậy của từng phương pháp
    if n >= 6:
        # Với đủ dữ liệu, ưu tiên linear regression và exponential smoothing
        w1 = 0.35  # Linear Regression
        w2 = 0.35  # Exponential Smoothing
        w3 = 0.15  # Weighted Average
        w4 = 0.15  # Momentum
    elif n >= 3:
        # Với ít dữ liệu hơn, ưu tiên exponential smoothing
        w1 = 0.25  # Linear Regression
        w2 = 0.40  # Exponential Smoothing
        w3 = 0.20  # Weighted Average
        w4 = 0.15  # Momentum
    else:
        # Với rất ít dữ liệu, dùng weighted average
        w1 = 0.20  # Linear Regression
        w2 = 0.30  # Exponential Smoothing
        w3 = 0.35  # Weighted Average
        w4 = 0.15  # Momentum
    
    # Kết hợp các dự đoán
    ensemble_prediction = (
        w1 * linear_prediction +
        w2 * exp_smoothing_prediction +
        w3 * weighted_avg +
        w4 * momentum_prediction
    )
    
    # Áp dụng seasonality factor
    final_prediction = ensemble_prediction * seasonality_factor
    
    # Đảm bảo dự đoán hợp lý (không quá khác biệt so với giá trị gần nhất)
    # Nếu dự đoán quá khác biệt, điều chỉnh về phía giá trị gần nhất
    last_value = expenses_array[-1]
    max_deviation = last_value * 0.5  # Cho phép sai lệch tối đa 50%
    if abs(final_prediction - last_value) > max_deviation:
        # Điều chỉnh về phía giá trị gần nhất với hệ số smoothing
        final_prediction = 0.7 * final_prediction + 0.3 * last_value
    
    return max(0, final_prediction)  # Đảm bảo không âm


def predict_expense_simple(expenses: List[float]) -> float:
    """Wrapper function - sử dụng mô hình AI mới"""
    return predict_expense_ai(expenses)


def predict_by_category(transactions: List[Dict]) -> Dict[str, float]:
    """Dự đoán chi tiêu theo từng danh mục"""
    # Nhóm theo danh mục và tháng
    category_monthly = defaultdict(lambda: defaultdict(float))
    
    for trans in transactions:
        if trans.get('type') == 'EXPENSE':
            category = trans.get('category', 'Khác')
            date_str = trans.get('date', '')
            if date_str:
                try:
                    # Parse date (có thể là ISO string hoặc timestamp)
                    if isinstance(date_str, str):
                        date = datetime.fromisoformat(date_str.replace('Z', '+00:00'))
                    else:
                        date = datetime.fromtimestamp(date_str)
                    month_key = f"{date.year}-{date.month:02d}"
                    category_monthly[category][month_key] += float(trans.get('amount', 0))
                except:
                    pass
    
    predictions = {}
    for category, monthly_data in category_monthly.items():
        amounts = sorted(monthly_data.items())
        if amounts:
            values = [amt for _, amt in amounts]
            predictions[category] = predict_expense_simple(values)
    
    return predictions


def calculate_warnings(current_month_total: float, average_expense: float, 
                       predicted_next: float, monthly_amounts: List[float], 
                       trend_percentage: float) -> tuple:
    """Tính toán cảnh báo chi tiêu"""
    warnings = []
    warning_level = 'none'  # 'none', 'low', 'medium', 'high'
    
    if current_month_total > 0 and average_expense > 0:
        # Cảnh báo 1: Chi tiêu hiện tại vượt quá trung bình
        excess_percentage = ((current_month_total - average_expense) / average_expense) * 100
        if excess_percentage > 50:
            warnings.append({
                'type': 'excess_average',
                'message': f'Chi tiêu tháng này vượt quá trung bình {excess_percentage:.1f}%',
                'severity': 'high' if excess_percentage > 100 else 'medium'
            })
            warning_level = 'high' if excess_percentage > 100 else 'medium'
        elif excess_percentage > 30:
            warnings.append({
                'type': 'excess_average',
                'message': f'Chi tiêu tháng này cao hơn trung bình {excess_percentage:.1f}%',
                'severity': 'low'
            })
            if warning_level == 'none':
                warning_level = 'low'
    
    if current_month_total > 0 and predicted_next > 0:
        # Cảnh báo 2: Chi tiêu hiện tại vượt quá dự đoán
        excess_prediction = ((current_month_total - predicted_next) / predicted_next) * 100
        if excess_prediction > 30:
            warnings.append({
                'type': 'excess_prediction',
                'message': f'Chi tiêu tháng này vượt quá dự đoán {excess_prediction:.1f}%',
                'severity': 'high' if excess_prediction > 50 else 'medium'
            })
            if warning_level in ['none', 'low']:
                warning_level = 'high' if excess_prediction > 50 else 'medium'
    
    if len(monthly_amounts) >= 2:
        if trend_percentage > 30:
            warnings.append({
                'type': 'trend_increase',
                'message': f'Xu hướng tăng mạnh {trend_percentage:.1f}% so với tháng trước',
                'severity': 'medium'
            })
            if warning_level == 'none':
                warning_level = 'medium'
    return warnings, warning_level

