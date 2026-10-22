"""Generate clearly fictional, text-extractable campus notice PDFs for demos."""

from pathlib import Path
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "output" / "pdf"
FONT = "SimHeiDemo"
pdfmetrics.registerFont(TTFont(FONT, r"C:\Windows\Fonts\simhei.ttf"))

NOTICES = [
    (
        "01-general-enrollment.pdf", "2027级新生报到通用说明", "QM-2027-001",
        [
            "报到时间：2027年9月3日 08:30-17:00。具体校区地点见适用通知。",
            "报到通用材料：录取通知书原件、居民身份证原件。",
            "校园卡：入校后在迎新服务台领取，领取时间为报到当日 08:30-17:00。",
            "请以本套演示资料中的校区专项通知核对地点，不适用其他学校。",
            "- [ ] 核对通用报到材料 | 材料：通知书、身份证 | 地点：迎新服务台 | 截止：9月3日17:00",
            "- [ ] 领取校园卡 | 材料：身份证 | 地点：迎新服务台 | 截止：9月3日17:00",
        ],
    ),
    (
        "02-east-undergraduate.pdf", "2027级东校区本科新生报到通知", "QM-2027-002",
        [
            "适用对象：东校区本科新生。西校区及研究生不适用本通知。",
            "东校区本科报到地点：综合服务中心A厅。报到时间：2027年9月3日 08:30-17:00。",
            "东校区本科报到须携带录取通知书原件和居民身份证原件。",
            "咨询窗口：综合服务中心A厅1号窗口；本套演示资料不提供真实电话。",
            "- [ ] 办理东校区本科报到 | 材料：通知书、身份证 | 地点：综合服务中心A厅 | 截止：9月3日17:00",
        ],
    ),
    (
        "03-west-graduate.pdf", "2027级西校区研究生报到通知", "QM-2027-003",
        [
            "适用对象：西校区研究生新生。东校区及本科新生不适用本通知。",
            "西校区研究生报到地点：研究生服务楼B201。报到时间：2027年9月4日 09:00-16:00。",
            "西校区研究生报到须携带录取通知书原件和居民身份证原件。",
            "- [ ] 办理西校区研究生报到 | 材料：通知书、身份证 | 地点：研究生服务楼B201 | 截止：9月4日16:00",
        ],
    ),
    (
        "04-east-housing.pdf", "2027级东校区住宿办理说明", "QM-2027-004",
        [
            "适用对象：东校区住宿生。非住宿生无需办理本通知所列事项。",
            "东校区住宿登记地点：松苑宿舍一层服务台。登记时间：2027年9月3日 10:00-18:00。",
            "住宿登记须出示居民身份证原件；宿舍床位信息以现场分配结果为准。",
            "- [ ] 办理东校区住宿登记 | 材料：身份证 | 地点：松苑宿舍服务台 | 截止：9月3日18:00",
        ],
    ),
    (
        "05-expired-payment.pdf", "2025级旧版收费方式通知", "QM-2025-009",
        [
            "本通知仅适用于2025级，已于2025年9月30日失效。",
            "旧版收费方式：线下现金窗口。此方式不得用于2027级新生。",
            "- [ ] 核对旧版收费方式 | 材料：现金 | 地点：旧版现金窗口 | 截止：2025年9月30日",
        ],
    ),
    (
        "06-shuttle-v1.pdf", "2027级校园接驳通知", "QM-2027-005-V1",
        [
            "旧版接驳路线：红线，从火车站到东校区北门。此版本用于验证更新替换。",
            "旧版接驳发车时间：2027年9月3日 07:00。",
            "- [ ] 乘坐旧版红线接驳车 | 材料：通知书 | 地点：火车站 | 截止：9月3日07:00",
        ],
    ),
    (
        "07-shuttle-v2.pdf", "2027级校园接驳通知", "QM-2027-005-V2",
        [
            "新版接驳路线：蓝线，从火车站到东校区南门。旧版红线已停止使用。",
            "新版接驳发车时间：2027年9月3日 07:30。",
            "- [ ] 乘坐新版蓝线接驳车 | 材料：通知书 | 地点：火车站 | 截止：9月3日07:30",
        ],
    ),
]


def wrap(text: str, max_width: float, font_size: int):
    lines, current = [], ""
    for character in text:
        if pdfmetrics.stringWidth(current + character, FONT, font_size) > max_width and current:
            lines.append(current)
            current = character
        else:
            current += character
    if current:
        lines.append(current)
    return lines


def generate():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    width, height = A4
    for filename, title, number, paragraphs in NOTICES:
        target = OUTPUT / filename
        pdf = canvas.Canvas(str(target), pagesize=A4, pageCompression=0)
        pdf.setTitle(title)
        pdf.setAuthor("启程演示项目")
        pdf.setFillColor(colors.HexColor("#153C31"))
        pdf.rect(0, height - 155, width, 155, fill=1, stroke=0)
        pdf.setFillColor(colors.HexColor("#B9DBCB"))
        pdf.setFont(FONT, 11)
        pdf.drawString(48, height - 55, "启程 · 虚构校园通知")
        pdf.setFillColor(colors.white)
        pdf.setFont(FONT, 20)
        pdf.drawString(48, height - 100, title)
        pdf.setFont(FONT, 10)
        pdf.drawString(48, height - 130, f"文件编号 {number}  |  启明大学（虚构）")

        y = height - 200
        pdf.setFillColor(colors.HexColor("#4D665A"))
        pdf.setFont(FONT, 10)
        pdf.drawString(48, y, "说明：本资料仅用于功能演示，不代表真实学校政策。")
        y -= 43
        for index, paragraph in enumerate(paragraphs, 1):
            if y < 95:
                pdf.showPage()
                y = height - 70
            checklist = paragraph.startswith("- [ ] ")
            if not checklist:
                pdf.setFillColor(colors.HexColor("#173D31"))
                pdf.setFont(FONT, 12)
                pdf.drawString(48, y, f"{index:02d}")
            pdf.setFillColor(colors.HexColor("#263C32"))
            font_size = 9 if checklist else 11
            pdf.setFont(FONT, font_size)
            start_x = 48 if checklist else 83
            for line in wrap(paragraph, width - start_x - 48, font_size):
                pdf.drawString(start_x, y, line)
                y -= 22
            y -= 18

        pdf.setStrokeColor(colors.HexColor("#D5E1D8"))
        pdf.line(48, 62, width - 48, 62)
        pdf.setFillColor(colors.HexColor("#697B70"))
        pdf.setFont(FONT, 9)
        pdf.drawString(48, 45, "SIMULATION ONLY / 仅供仿真测试")
        pdf.drawRightString(width - 48, 45, number)
        pdf.save()
        print(target)


if __name__ == "__main__":
    generate()
