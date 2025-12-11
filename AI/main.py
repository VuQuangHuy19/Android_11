from flask import Flask, request, jsonify, render_template
from flask_cors import CORS
from datetime import datetime, timedelta
from collections import defaultdict
import numpy as np
from typing import List, Dict

from ai_models import predict_expense_simple, predict_by_category, calculate_warnings, predict_expense_ai

app = Flask(__name__, template_folder='templates')
CORS(app)  # Cho phép CORS để app Android có thể gọi


@app.route('/')
def index():
    """Trang chủ - hiển thị web interface"""
    return render_template('index.html')


@app.route('/api/predict', methods=['GET', 'POST'])
def predict():
    """API endpoint để dự đoán chi tiêu"""
    try:
        # Nhận dữ liệu từ request
        if request.method == 'POST':
            data = request.get_json()
            transactions = data.get('transactions', [])
        else:
            # GET request - chỉ dùng sample data nếu không có dữ liệu từ query params
            # App Android sẽ gửi POST với dữ liệu thật
            transactions = []
            # Chỉ dùng sample data cho web interface demo
            use_sample = request.args.get('sample', 'false').lower() == 'true'
            if use_sample:
                transactions = generate_sample_data()
        expenses = [t for t in transactions if t.get('type') == 'EXPENSE']      
        if not expenses:
            return jsonify({
                'current_month_total': 0,
                'predicted_next_month': 0,
                'average_expense': 0,
                'trend': 'Không có dữ liệu',
                'trend_percentage': 0,
                'monthly_data': [],
                'predicted_data': [],
                'category_predictions': [],
                'warnings': [],
                'warning_level': 'none'
            })
        monthly_expenses = defaultdict(float)
        current_date = datetime.now()
        for exp in expenses:
            date_str = exp.get('date', '')
            if date_str:
                try:
                    if isinstance(date_str, str):
                        date = datetime.fromisoformat(date_str.replace('Z', '+00:00'))
                    else:
                        date = datetime.fromtimestamp(date_str)
                    month_key = f"{date.year}-{date.month:02d}"
                    monthly_expenses[month_key] += float(exp.get('amount', 0))
                except:
                    pass
        
        # Sắp xếp theo tháng
        sorted_months = sorted(monthly_expenses.items())
        monthly_amounts = [amt for _, amt in sorted_months]
        month_labels = [month for month, _ in sorted_months]
        
        # Dự đoán tháng tiếp theo
        predicted_next = predict_expense_simple(monthly_amounts) if monthly_amounts else 0
        
        # Tính tháng hiện tại
        current_month_key = f"{current_date.year}-{current_date.month:02d}"
        current_month_total = monthly_expenses.get(current_month_key, 0)
        
        # Tính trung bình
        average_expense = np.mean(monthly_amounts) if monthly_amounts else 0
        
        # Tính xu hướng
        if len(monthly_amounts) >= 2:
            trend_value = monthly_amounts[-1] - monthly_amounts[-2]
            trend_percentage = ((trend_value / monthly_amounts[-2]) * 100) if monthly_amounts[-2] > 0 else 0
            if trend_value > 0:
                trend = 'Tăng'
            elif trend_value < 0:
                trend = 'Giảm'
            else:
                trend = 'Ổn định'
        else:
            trend = 'Chưa đủ dữ liệu'
            trend_percentage = 0
        
        # Dự đoán theo danh mục
        category_predictions_dict = predict_by_category(expenses)
        category_predictions = [
            {'category': cat, 'predicted_amount': float(amt)}
            for cat, amt in sorted(category_predictions_dict.items(), key=lambda x: x[1], reverse=True)
        ]
        
        # Tạo dữ liệu dự đoán cho biểu đồ (3 tháng tiếp theo) bằng mô hình AI
        predicted_months = []
        predicted_amounts = []
        
        # Xác định tháng cuối cùng có dữ liệu
        if sorted_months:
            last_month = sorted_months[-1][0]
            year, month = map(int, last_month.split('-'))
        else:
            # Nếu không có dữ liệu, bắt đầu từ tháng hiện tại
            year = current_date.year
            month = current_date.month
        
        # Dự đoán tuần tự từng tháng (mỗi tháng sau dựa trên dữ liệu + dự đoán trước đó)
        working_data = monthly_amounts.copy()  # Bắt đầu với dữ liệu thực tế
        
        # Đảm bảo luôn có ít nhất 3 tháng dự đoán
        for i in range(1, 4):  # 3 tháng tiếp theo
            month += 1
            if month > 12:
                month = 1
                year += 1
            predicted_months.append(f"{year}-{month:02d}")
            
            # Sử dụng mô hình AI để dự đoán tháng tiếp theo
            if len(working_data) > 0:
                # Dự đoán dựa trên dữ liệu hiện có (bao gồm cả các dự đoán trước đó)
                month_prediction = predict_expense_ai(working_data)
                predicted_amounts.append(max(0, month_prediction))
                # Thêm dự đoán vào working_data để dự đoán tháng tiếp theo
                working_data.append(month_prediction)
            else:
                # Nếu không có dữ liệu, dùng giá trị dự đoán chung
                base_prediction = predicted_next if predicted_next > 0 else 1000000  # Giá trị mặc định
                predicted_amounts.append(max(0, base_prediction))
                working_data.append(base_prediction)
        
        # Tính toán cảnh báo (để trả về cho báo cáo Android)
        warnings, warning_level = calculate_warnings(
            current_month_total, average_expense, predicted_next, 
            monthly_amounts, trend_percentage
        )
        
        # Format dữ liệu trả về
        response_data = {
            'current_month_total': float(current_month_total),
            'predicted_next_month': float(predicted_next),
            'average_expense': float(average_expense),
            'trend': trend,
            'trend_percentage': round(trend_percentage, 1) if trend_percentage != 0 else None,
            'monthly_data': [
                {'month': month, 'amount': float(amt)}
                for month, amt in zip(month_labels, monthly_amounts)
            ],
            'predicted_data': [
                {'month': month, 'amount': float(amt)}
                for month, amt in zip(predicted_months, predicted_amounts)
            ],
            'category_predictions': category_predictions,
            'warnings': warnings,
            'warning_level': warning_level
        }
        
        return jsonify(response_data)
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500


def generate_sample_data():
    """Tạo dữ liệu mẫu để demo"""
    sample_data = []
    base_date = datetime.now() - timedelta(days=90)
    categories = ['Ăn uống', 'Di chuyển', 'Mua sắm', 'Giải trí', 'Học tập', 'Sức khỏe']
    for i in range(60):  # 60 giao dịch trong 3 tháng
        date = base_date + timedelta(days=i * 1.5)
        category = categories[i % len(categories)]
        amount = np.random.uniform(50000, 500000)
        sample_data.append({
            'type': 'EXPENSE',
            'amount': amount,
            'category': category,
            'date': date.isoformat()
        }) 
    return sample_data
if __name__ == '__main__':
    print("🚀 Khởi động server dự đoán chi tiêu...")
    print("📊 Truy cập http://localhost:5000 để xem web interface")
    print("🔌 API endpoint: http://localhost:5000/api/predict")
    app.run(host='0.0.0.0', port=5000, debug=True)
