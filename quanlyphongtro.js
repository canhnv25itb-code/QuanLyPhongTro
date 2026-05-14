import { useState, useMemo, useEffect, useRef } from "react";
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, AreaChart, Area, CartesianGrid, Legend,
} from "recharts";

/* ══════════════════════════════════════════════════════════════
   SEED DATA
══════════════════════════════════════════════════════════════ */
const USERS = [
  { id: "admin1", name: "Admin Hệ Thống", role: "admin", email: "admin@rentalhub.vn", password: "admin123", avatar: "A" },
  { id: "host1",  name: "Nguyễn Thành Long", role: "host",  email: "long@rentalhub.vn",  password: "host123",  avatar: "L", building: "Sơn Trà" },
  { id: "host2",  name: "Trần Thị Mai",      role: "host",  email: "mai@rentalhub.vn",   password: "host123",  avatar: "M", building: "Mỹ An" },
  { id: "t1",     name: "Nguyễn Văn An",     role: "tenant",email: "an@mail.com",         password: "123456",   avatar: "A", roomId: "P101" },
  { id: "t2",     name: "Trần Thị Bình",     role: "tenant",email: "binh@mail.com",       password: "123456",   avatar: "B", roomId: "P102" },
  { id: "t3",     name: "Lê Hoàng Cường",    role: "tenant",email: "cuong@mail.com",      password: "123456",   avatar: "C", roomId: "P201" },
];

const INIT_ROOMS = [
  { id:"P101",building:"Sơn Trà",hostId:"host1",floor:1,tenant:"Nguyễn Văn An",tenantId:"t1",phone:"091 234 5678",rent:3500000,status:"occupied",paid:true, electricOld:160,electricNew:180,water:12,serviceFee:100000,deposit:7000000,moveIn:"01/01/2025" },
  { id:"P102",building:"Sơn Trà",hostId:"host1",floor:1,tenant:"Trần Thị Bình",tenantId:"t2",phone:"092 345 6789",rent:3500000,status:"occupied",paid:false,electricOld:195,electricNew:220,water:15,serviceFee:100000,deposit:7000000,moveIn:"15/02/2025" },
  { id:"P103",building:"Sơn Trà",hostId:"host1",floor:1,tenant:"",tenantId:null,phone:"",rent:3200000,status:"empty",paid:false,electricOld:0,electricNew:0,water:0,serviceFee:100000,deposit:0,moveIn:"" },
  { id:"P104",building:"Sơn Trà",hostId:"host1",floor:1,tenant:"",tenantId:null,phone:"",rent:3500000,status:"maintenance",paid:false,electricOld:0,electricNew:0,water:0,serviceFee:100000,deposit:0,moveIn:"" },
  { id:"P201",building:"Sơn Trà",hostId:"host1",floor:2,tenant:"Lê Hoàng Cường",tenantId:"t3",phone:"093 456 7890",rent:4000000,status:"occupied",paid:true, electricOld:170,electricNew:195,water:13,serviceFee:150000,deposit:8000000,moveIn:"01/03/2025" },
  { id:"P202",building:"Sơn Trà",hostId:"host1",floor:2,tenant:"Phạm Minh Đức",tenantId:null,phone:"094 567 8901",rent:4000000,status:"occupied",paid:false,electricOld:185,electricNew:210,water:14,serviceFee:150000,deposit:8000000,moveIn:"01/04/2025" },
  { id:"P203",building:"Sơn Trà",hostId:"host1",floor:2,tenant:"Hoàng Thị Em",tenantId:null,phone:"095 678 9012",rent:4200000,status:"occupied",paid:true, electricOld:150,electricNew:175,water:11,serviceFee:150000,deposit:8400000,moveIn:"15/01/2025" },
  { id:"P204",building:"Sơn Trà",hostId:"host1",floor:2,tenant:"",tenantId:null,phone:"",rent:4000000,status:"empty",paid:false,electricOld:0,electricNew:0,water:0,serviceFee:150000,deposit:0,moveIn:"" },
  { id:"P301",building:"Sơn Trà",hostId:"host1",floor:3,tenant:"Vũ Văn Phong",tenantId:null,phone:"096 789 0123",rent:4500000,status:"occupied",paid:true, electricOld:200,electricNew:230,water:16,serviceFee:200000,deposit:9000000,moveIn:"01/02/2025" },
  { id:"P302",building:"Sơn Trà",hostId:"host1",floor:3,tenant:"Đặng Thị Giang",tenantId:null,phone:"097 890 1234",rent:4500000,status:"occupied",paid:false,electricOld:175,electricNew:200,water:13,serviceFee:200000,deposit:9000000,moveIn:"01/03/2025" },
  { id:"P303",building:"Sơn Trà",hostId:"host1",floor:3,tenant:"",tenantId:null,phone:"",rent:4200000,status:"empty",paid:false,electricOld:0,electricNew:0,water:0,serviceFee:200000,deposit:0,moveIn:"" },
  { id:"P304",building:"Sơn Trà",hostId:"host1",floor:3,tenant:"Bùi Văn Hải",tenantId:null,phone:"098 901 2345",rent:4800000,status:"occupied",paid:true, electricOld:165,electricNew:190,water:14,serviceFee:200000,deposit:9600000,moveIn:"15/01/2025" },
  { id:"MA101",building:"Mỹ An",hostId:"host2",floor:1,tenant:"Phan Thị Lan",tenantId:null,phone:"099 012 3456",rent:3800000,status:"occupied",paid:true, electricOld:140,electricNew:168,water:10,serviceFee:120000,deposit:7600000,moveIn:"01/01/2025" },
  { id:"MA102",building:"Mỹ An",hostId:"host2",floor:1,tenant:"",tenantId:null,phone:"",rent:3600000,status:"empty",paid:false,electricOld:0,electricNew:0,water:0,serviceFee:120000,deposit:0,moveIn:"" },
  { id:"MA201",building:"Mỹ An",hostId:"host2",floor:2,tenant:"Đinh Văn Nam",tenantId:null,phone:"088 123 4567",rent:4300000,status:"occupied",paid:false,electricOld:190,electricNew:215,water:12,serviceFee:150000,deposit:8600000,moveIn:"01/02/2025" },
];

const ELEC_PRICE = 3800; // đồng/kWh
const WATER_PRICE = 18000; // đồng/m³

const calcBill = (r) => ({
  rent: r.rent,
  electric: (r.electricNew - r.electricOld) * ELEC_PRICE,
  water: r.water * WATER_PRICE,
  service: r.serviceFee,
  total: r.rent + (r.electricNew - r.electricOld) * ELEC_PRICE + r.water * WATER_PRICE + r.serviceFee,
  kWh: r.electricNew - r.electricOld,
});

const INIT_TICKETS = [
  { id:"TK001",roomId:"P102",building:"Sơn Trà",tenant:"Trần Thị Bình",issue:"Điều hòa không hoạt động",cat:"Điện lạnh",status:"pending",date:"09/06/2025",priority:"high",desc:"Điều hòa bật không lên, có tiếng kêu lạ",history:[{date:"09/06/2025",action:"Tạo ticket",by:"Trần Thị Bình"}] },
  { id:"TK002",roomId:"P201",building:"Sơn Trà",tenant:"Lê Hoàng Cường",issue:"Vòi nước bị rỉ sét",cat:"Nước",status:"in_progress",date:"07/06/2025",priority:"medium",desc:"Vòi nước bếp bị rỉ rất nhiều",history:[{date:"07/06/2025",action:"Tạo ticket",by:"Lê Hoàng Cường"},{date:"08/06/2025",action:"Chủ trọ bắt đầu xử lý",by:"Nguyễn Thành Long"}] },
  { id:"TK003",roomId:"P301",building:"Sơn Trà",tenant:"Vũ Văn Phong",issue:"Ổ khóa cửa bị hỏng",cat:"Cơ sở vật chất",status:"done",date:"05/06/2025",priority:"high",desc:"Không mở khóa được",repairCost:350000,history:[{date:"05/06/2025",action:"Tạo ticket",by:"Vũ Văn Phong"},{date:"05/06/2025",action:"Chủ trọ bắt đầu xử lý",by:"Nguyễn Thành Long"},{date:"06/06/2025",action:"Hoàn thành sửa chữa. Chi phí: 350,000₫",by:"Nguyễn Thành Long"}] },
  { id:"TK004",roomId:"P203",building:"Sơn Trà",tenant:"Hoàng Thị Em",issue:"Đèn phòng tắm nhấp nháy",cat:"Điện",status:"pending",date:"09/06/2025",priority:"low",desc:"Bóng đèn phòng tắm chập chờn",history:[{date:"09/06/2025",action:"Tạo ticket",by:"Hoàng Thị Em"}] },
  { id:"TK005",roomId:"P304",building:"Sơn Trà",tenant:"Bùi Văn Hải",issue:"Tắc đường ống thoát nước",cat:"Nước",status:"in_progress",date:"06/06/2025",priority:"medium",desc:"Nhà tắm thoát nước rất chậm",history:[{date:"06/06/2025",action:"Tạo ticket",by:"Bùi Văn Hải"},{date:"07/06/2025",action:"Đang chờ thợ đến",by:"Nguyễn Thành Long"}] },
  { id:"TK006",roomId:"MA101",building:"Mỹ An",tenant:"Phan Thị Lan",issue:"Cửa sổ không đóng kín",cat:"Cơ sở vật chất",status:"pending",date:"08/06/2025",priority:"low",desc:"Khe hở lớn, gió thổi vào nhiều",history:[{date:"08/06/2025",action:"Tạo ticket",by:"Phan Thị Lan"}] },
];

const INIT_INVENTORY = {
  "P101": [
    { id:1, item:"Điều hòa Daikin 9000BTU", qty:1, condition:"Tốt", note:"Mua 01/2024" },
    { id:2, item:"Giường đôi 1m6", qty:1, condition:"Tốt", note:"" },
    { id:3, item:"Tủ quần áo 3 cánh", qty:1, condition:"Tốt", note:"" },
    { id:4, item:"Bàn học + ghế", qty:1, condition:"Bình thường", note:"Ghế bị lung lay" },
    { id:5, item:"Tủ lạnh mini", qty:1, condition:"Tốt", note:"" },
  ],
  "P102": [
    { id:1, item:"Điều hòa Panasonic 12000BTU", qty:1, condition:"Tốt", note:"" },
    { id:2, item:"Giường đơn 1m2", qty:1, condition:"Tốt", note:"" },
    { id:3, item:"Tủ quần áo 2 cánh", qty:1, condition:"Bình thường", note:"Cánh tủ bị cong" },
    { id:4, item:"Bàn học + ghế", qty:1, condition:"Tốt", note:"" },
  ],
  "P201": [
    { id:1, item:"Điều hòa Mitsubishi 12000BTU", qty:1, condition:"Tốt", note:"Bảo hành đến 12/2026" },
    { id:2, item:"Giường đôi 1m8", qty:1, condition:"Tốt", note:"" },
    { id:3, item:"Tủ quần áo 4 cánh", qty:1, condition:"Tốt", note:"" },
    { id:4, item:"Sofa nhỏ", qty:1, condition:"Tốt", note:"" },
    { id:5, item:"Tủ lạnh Panasonic 150L", qty:1, condition:"Tốt", note:"" },
    { id:6, item:"Máy giặt Samsung 8kg", qty:1, condition:"Tốt", note:"" },
  ],
};

const REVENUE_DATA = [
  { m:"T1", rev:32.5, occ:75, target:40 },
  { m:"T2", rev:34.2, occ:75, target:40 },
  { m:"T3", rev:38.0, occ:83, target:42 },
  { m:"T4", rev:35.8, occ:83, target:42 },
  { m:"T5", rev:41.3, occ:92, target:45 },
  { m:"T6", rev:44.0, occ:92, target:45 },
];

const INIT_REVIEWS = [
  { id:1,tenant:"Nguyễn Văn An",roomId:"P101",rating:5,sec:5,util:4,land:5,comment:"Chủ trọ rất nhiệt tình, phòng sạch thoáng mát! Sẽ ở lâu dài.",date:"01/06/2025" },
  { id:2,tenant:"Trần Minh Khoa",roomId:"P203",rating:4,sec:4,util:3,land:5,comment:"Vị trí tốt gần ĐH, điện nước ổn định. Giá hơi cao so với khu vực.",date:"15/05/2025" },
  { id:3,tenant:"Lê Thị Hoa",roomId:"P304",rating:3,sec:4,util:3,land:4,comment:"Phòng ổn nhưng hơi nóng vào mùa hè, cần thêm quạt trần.",date:"20/04/2025" },
];

/* ══════════════════════════════════════════════════════════════
   THEME & UTILS
══════════════════════════════════════════════════════════════ */
const fmt = (n) => new Intl.NumberFormat("vi-VN").format(Math.round(n)) + " ₫";
const fmtM = (n) => (n / 1000000).toFixed(1) + "tr";

function useTheme(dark) {
  return {
    bg:      dark ? "#0A0E17" : "#F4F6FA",
    bg2:     dark ? "#111827" : "#FFFFFF",
    bg3:     dark ? "#1A2235" : "#EEF1F8",
    bg4:     dark ? "#242F44" : "#E3E8F4",
    border:  dark ? "#2D3A52" : "#D1D9EE",
    text:    dark ? "#E8EDF5" : "#1A2035",
    muted:   dark ? "#7A8BA8" : "#5A6A8A",
    accent:  "#F59E0B",
    accent2: "#3B82F6",
    accentDim: dark ? "#78350F33" : "#FEF3C7",
    green:   dark ? "#34D399" : "#059669",
    red:     dark ? "#F87171" : "#DC2626",
    blue:    dark ? "#60A5FA" : "#2563EB",
    purple:  dark ? "#C084FC" : "#7C3AED",
    orange:  dark ? "#FB923C" : "#EA580C",
    greenDim: dark ? "#064E3B44" : "#D1FAE5",
    redDim:   dark ? "#7F1D1D44" : "#FEE2E2",
    blueDim:  dark ? "#1E3A8A44" : "#DBEAFE",
    purpleDim:dark ? "#4C1D9544" : "#EDE9FE",
    orangeDim:dark ? "#7C2D1244" : "#FED7AA",
  };
}

/* ══════════════════════════════════════════════════════════════
   BASE COMPONENTS
══════════════════════════════════════════════════════════════ */
function Avatar({ name="?", size=32, bg="#F59E0B", color="#7C2D12" }) {
  return (
    <div style={{ width:size, height:size, borderRadius:"50%", background:bg, color, display:"flex", alignItems:"center", justifyContent:"center", fontWeight:800, fontSize:size*0.4, flexShrink:0, fontFamily:"monospace" }}>
      {(name||"?").charAt(0).toUpperCase()}
    </div>
  );
}

function Stars({ v=5, size=14 }) {
  return <span style={{ fontSize:size, letterSpacing:1 }}>{[1,2,3,4,5].map(i=><span key={i} style={{color:i<=v?"#F59E0B":"#4A5568"}}>★</span>)}</span>;
}

function Badge({ label, color, dim, small }) {
  return <span style={{ background:dim||"#333", color:color||"#fff", fontSize:small?10:11, fontWeight:700, padding:small?"2px 6px":"3px 9px", borderRadius:20, letterSpacing:0.3, whiteSpace:"nowrap" }}>{label}</span>;
}

function StatusBadge({ status, t }) {
  const map = {
    occupied:    { label:"Có người", color:t.green,  dim:t.greenDim },
    empty:       { label:"Trống",    color:t.blue,   dim:t.blueDim },
    maintenance: { label:"Bảo trì", color:t.orange, dim:t.orangeDim },
    pending:     { label:"Chờ xử lý",color:t.orange, dim:t.orangeDim },
    in_progress: { label:"Đang sửa", color:t.blue,   dim:t.blueDim },
    done:        { label:"Hoàn thành",color:t.green, dim:t.greenDim },
  };
  const s = map[status]||map.pending;
  return <Badge label={s.label} color={s.color} dim={s.dim} />;
}

function PriorityDot({ priority, t }) {
  const c = priority==="high"?t.red:priority==="medium"?t.orange:t.muted;
  return <div style={{ width:8,height:8,borderRadius:"50%",background:c,flexShrink:0 }}/>;
}

function Card({ children, t, p="16px 20px", mb=16, style={} }) {
  return (
    <div style={{ background:t.bg2, border:`1px solid ${t.border}`, borderRadius:14, padding:p, marginBottom:mb, ...style }}>
      {children}
    </div>
  );
}

function StatCard({ icon, label, value, sub, t, accent, onClick }) {
  return (
    <div onClick={onClick} style={{ background:t.bg2, border:`1px solid ${t.border}`, borderRadius:14, padding:"16px 18px", flex:1, minWidth:0, cursor:onClick?"pointer":"default", transition:"transform 0.15s", position:"relative", overflow:"hidden" }}>
      <div style={{ position:"absolute", top:-8, right:-8, fontSize:48, opacity:0.07 }}>{icon}</div>
      <div style={{ fontSize:22, marginBottom:4 }}>{icon}</div>
      <div style={{ fontSize:22, fontWeight:800, color:accent||t.text, letterSpacing:-0.5 }}>{value}</div>
      <div style={{ fontSize:12, color:t.muted, marginTop:2, fontWeight:500 }}>{label}</div>
      {sub && <div style={{ fontSize:11, color:t.muted, marginTop:4 }}>{sub}</div>}
    </div>
  );
}

function SectionTitle({ children, t, action }) {
  return (
    <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:14 }}>
      <h3 style={{ color:t.text, fontSize:14, fontWeight:800, margin:0, letterSpacing:0.2 }}>{children}</h3>
      {action}
    </div>
  );
}

function Btn({ children, onClick, variant="primary", t, small, disabled, full }) {
  const colors = {
    primary: { bg:t.accent, color:"#1C1917", border:"none" },
    secondary:{ bg:t.bg3, color:t.text, border:`1px solid ${t.border}` },
    danger:   { bg:t.red, color:"#fff", border:"none" },
    ghost:    { bg:"transparent", color:t.muted, border:`1px solid ${t.border}` },
    success:  { bg:t.green, color:"#fff", border:"none" },
  };
  const c = colors[variant]||colors.primary;
  return (
    <button onClick={onClick} disabled={disabled} style={{
      padding:small?"6px 14px":"9px 18px",
      borderRadius:9, border:c.border,
      background:disabled?"#333":c.bg, color:disabled?t.muted:c.color,
      fontWeight:700, fontSize:small?11:13, cursor:disabled?"not-allowed":"pointer",
      opacity:disabled?0.55:1, transition:"all 0.15s",
      width:full?"100%":"auto", letterSpacing:0.2,
    }}>{children}</button>
  );
}

function Input({ value, onChange, placeholder, t, type="text", as="input", min, max }) {
  const style = { width:"100%", boxSizing:"border-box", padding:"9px 12px", borderRadius:9, border:`1px solid ${t.border}`, background:t.bg3, color:t.text, fontSize:13, outline:"none", resize:"vertical", fontFamily:"inherit" };
  return as==="textarea"
    ? <textarea value={value} onChange={onChange} placeholder={placeholder} rows={3} style={style}/>
    : <input type={type} value={value} onChange={onChange} placeholder={placeholder} style={style} min={min} max={max}/>;
}

function Select({ value, onChange, options, t }) {
  return (
    <select value={value} onChange={onChange} style={{ padding:"9px 12px", borderRadius:9, border:`1px solid ${t.border}`, background:t.bg3, color:t.text, fontSize:13, width:"100%", boxSizing:"border-box", fontFamily:"inherit" }}>
      {options.map(o=><option key={o.v} value={o.v}>{o.l}</option>)}
    </select>
  );
}

function Tabs({ tabs, active, setActive, t }) {
  return (
    <div style={{ display:"flex", gap:4, background:t.bg3, borderRadius:12, padding:4, marginBottom:20, overflowX:"auto", flexShrink:0 }}>
      {tabs.map(tab=>(
        <button key={tab.id} onClick={()=>setActive(tab.id)} style={{
          padding:"8px 14px", borderRadius:9, border:"none",
          background:active===tab.id?t.bg2:"transparent",
          color:active===tab.id?t.text:t.muted,
          fontWeight:active===tab.id?700:500,
          fontSize:13, cursor:"pointer", whiteSpace:"nowrap",
          boxShadow:active===tab.id?`0 1px 4px rgba(0,0,0,.2)`:"none",
          transition:"all 0.15s", flexShrink:0,
        }}>
          {tab.icon} {tab.label}
        </button>
      ))}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   QR CODE (decorative)
══════════════════════════════════════════════════════════════ */
function QRCode({ value, size=100 }) {
  const n=21, cell=Math.floor(size/n);
  const seed=value.split("").reduce((a,c)=>(a*31+c.charCodeAt(0))&0xFFFFFF,0);
  const pseudo=(i)=>{const x=Math.sin(seed*9301+i*49297+233)*49979;return x-Math.floor(x);};
  const cells=Array.from({length:n*n},(_,i)=>{
    const r=Math.floor(i/n),c=i%n;
    const inF1=r<7&&c<7,inF2=r<7&&c>=n-7,inF3=r>=n-7&&c<7;
    if(inF1||inF2||inF3){const nr=inF3?r-(n-7):r,nc=inF2?c-(n-7):c;const rr=Math.min(nr,6),cc=Math.min(nc,6);if(rr===0||rr===6||cc===0||cc===6)return true;if(rr>=2&&rr<=4&&cc>=2&&cc<=4)return true;return false;}
    return pseudo(i)>0.42;
  });
  return (
    <div style={{display:"inline-block",background:"#fff",padding:8,borderRadius:10,lineHeight:0}}>
      {Array.from({length:n},(_,r)=>(
        <div key={r} style={{display:"flex"}}>
          {Array.from({length:n},(_,c)=>(
            <div key={c} style={{width:cell,height:cell,background:cells[r*n+c]?"#0A0E17":"#fff"}}/>
          ))}
        </div>
      ))}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   MODAL
══════════════════════════════════════════════════════════════ */
function Modal({ open, onClose, title, children, t, width=540 }) {
  if (!open) return null;
  return (
    <div style={{ position:"fixed",inset:0,background:"rgba(0,0,0,0.7)",zIndex:1000,display:"flex",alignItems:"center",justifyContent:"center",padding:16 }} onClick={onClose}>
      <div onClick={e=>e.stopPropagation()} style={{ background:t.bg2,border:`1px solid ${t.border}`,borderRadius:16,width:"100%",maxWidth:width,maxHeight:"90vh",overflowY:"auto",boxShadow:"0 25px 60px rgba(0,0,0,0.4)" }}>
        <div style={{ padding:"18px 22px",borderBottom:`1px solid ${t.border}`,display:"flex",justifyContent:"space-between",alignItems:"center",position:"sticky",top:0,background:t.bg2,zIndex:1 }}>
          <span style={{ fontSize:15,fontWeight:800,color:t.text }}>{title}</span>
          <button onClick={onClose} style={{ background:"transparent",border:"none",color:t.muted,fontSize:20,cursor:"pointer",lineHeight:1,padding:"0 4px" }}>✕</button>
        </div>
        <div style={{ padding:"20px 22px" }}>{children}</div>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   INVOICE MODAL
══════════════════════════════════════════════════════════════ */
function InvoiceModal({ room, open, onClose, t }) {
  if (!room) return null;
  const bill = calcBill(room);
  const month = "Tháng 6/2025";
  return (
    <Modal open={open} onClose={onClose} title={`🧾 Hóa đơn ${room.id} — ${month}`} t={t} width={480}>
      <div style={{ background:t.bg3,borderRadius:12,padding:"16px 20px",marginBottom:16 }}>
        <div style={{ display:"flex",justifyContent:"space-between",alignItems:"flex-start" }}>
          <div>
            <div style={{ fontSize:20,fontWeight:900,color:t.accent }}>🏡 The Rental Hub</div>
            <div style={{ fontSize:12,color:t.muted,marginTop:2 }}>123 Hoàng Diệu, Sơn Trà, Đà Nẵng</div>
            <div style={{ fontSize:12,color:t.muted }}>contact@rentalhub.vn</div>
          </div>
          <QRCode value={`phong-${room.id}-t6-2025-${bill.total}`} size={70}/>
        </div>
      </div>

      <div style={{ marginBottom:16 }}>
        <div style={{ display:"grid",gridTemplateColumns:"1fr 1fr",gap:12,marginBottom:12 }}>
          {[["Phòng",room.id],["Người thuê",room.tenant||"—"],["Kỳ",month],["Hạn nộp","15/06/2025"]].map(([k,v])=>(
            <div key={k}>
              <div style={{ fontSize:11,color:t.muted,marginBottom:2 }}>{k}</div>
              <div style={{ fontSize:13,fontWeight:700,color:t.text }}>{v}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ borderTop:`1px solid ${t.border}`,paddingTop:14 }}>
        {[
          ["🏠 Tiền phòng",fmt(bill.rent)],
          [`⚡ Điện (${bill.kWh} kWh × ${fmt(ELEC_PRICE).replace(" ₫","")})`,fmt(bill.electric)],
          [`💧 Nước (${room.water} m³ × ${fmt(WATER_PRICE).replace(" ₫","")})`,fmt(bill.water)],
          ["🛎 Phí dịch vụ",fmt(bill.service)],
        ].map(([k,v])=>(
          <div key={k} style={{ display:"flex",justifyContent:"space-between",padding:"9px 0",borderBottom:`1px dashed ${t.border}` }}>
            <span style={{ fontSize:13,color:t.muted }}>{k}</span>
            <span style={{ fontSize:13,fontWeight:600,color:t.text }}>{v}</span>
          </div>
        ))}
        <div style={{ display:"flex",justifyContent:"space-between",padding:"14px 0 4px",marginTop:4 }}>
          <span style={{ fontSize:15,fontWeight:800,color:t.text }}>TỔNG CỘNG</span>
          <span style={{ fontSize:20,fontWeight:900,color:t.accent }}>{fmt(bill.total)}</span>
        </div>
      </div>

      <div style={{ marginTop:16,padding:"10px 14px",borderRadius:10,background:room.paid?t.greenDim:t.redDim,fontSize:13,fontWeight:700,color:room.paid?t.green:t.red,textAlign:"center" }}>
        {room.paid?"✅ ĐÃ THANH TOÁN":"⚠️ CHƯA THANH TOÁN — Hệ thống tự động chốt sổ ngày 30/06"}
      </div>

      <div style={{ marginTop:16,padding:"10px 14px",borderRadius:10,background:t.blueDim,fontSize:12,color:t.blue }}>
        📱 Quét mã QR để thanh toán qua MoMo / ZaloPay / VNPay
      </div>
    </Modal>
  );
}

/* ══════════════════════════════════════════════════════════════
   INVENTORY MODAL
══════════════════════════════════════════════════════════════ */
function InventoryModal({ roomId, open, onClose, t }) {
  const items = INIT_INVENTORY[roomId] || [];
  const condColor = { "Tốt":t.green, "Bình thường":t.orange, "Hỏng":t.red };
  return (
    <Modal open={open} onClose={onClose} title={`📦 Kiểm kê tài sản — Phòng ${roomId}`} t={t}>
      {items.length===0
        ? <div style={{ textAlign:"center",color:t.muted,padding:"30px 0",fontSize:13 }}>Chưa có dữ liệu tài sản</div>
        : <>
          <div style={{ display:"grid",gap:8,marginBottom:16 }}>
            {items.map(item=>(
              <div key={item.id} style={{ display:"flex",justifyContent:"space-between",alignItems:"center",padding:"10px 14px",background:t.bg3,borderRadius:10 }}>
                <div>
                  <div style={{ fontSize:13,fontWeight:700,color:t.text }}>{item.item}</div>
                  {item.note&&<div style={{ fontSize:11,color:t.muted,marginTop:2 }}>{item.note}</div>}
                </div>
                <div style={{ display:"flex",alignItems:"center",gap:10 }}>
                  <span style={{ fontSize:12,color:t.muted }}>x{item.qty}</span>
                  <Badge label={item.condition} color={condColor[item.condition]||t.muted} dim={(condColor[item.condition]||t.muted)+"22"} small/>
                </div>
              </div>
            ))}
          </div>
          <div style={{ padding:"12px 16px",borderRadius:10,background:t.bg3,border:`1px dashed ${t.border}` }}>
            <div style={{ fontSize:12,fontWeight:700,color:t.muted,marginBottom:6 }}>📋 CHECKLIST TRƯỚC KHI BÀN GIAO</div>
            {items.map(item=>(
              <div key={item.id} style={{ display:"flex",alignItems:"center",gap:8,padding:"5px 0",borderBottom:`1px solid ${t.border}` }}>
                <div style={{ width:14,height:14,border:`2px solid ${t.border}`,borderRadius:3 }}/>
                <span style={{ fontSize:12,color:t.muted,flex:1 }}>{item.item} (x{item.qty})</span>
                <span style={{ fontSize:11,color:condColor[item.condition]||t.muted }}>{item.condition}</span>
              </div>
            ))}
          </div>
        </>
      }
    </Modal>
  );
}

/* ══════════════════════════════════════════════════════════════
   FLOOR PLAN
══════════════════════════════════════════════════════════════ */
function FloorPlan({ rooms, t, onRoomClick }) {
  const floors = [...new Set(rooms.map(r=>r.floor))].sort();
  const statusColor = {
    occupied: (paid) => paid ? "#059669" : "#DC2626",
    empty: () => "#2563EB",
    maintenance: () => "#D97706",
  };
  return (
    <Card t={t} p="16px" mb={16}>
      <SectionTitle t={t}>🏢 Sơ đồ tầng</SectionTitle>
      <div style={{ display:"flex",gap:16,marginBottom:14,flexWrap:"wrap" }}>
        {[["#059669","Đã thuê + Đã thu"],["#DC2626","Đã thuê + Chưa thu"],["#2563EB","Trống"],["#D97706","Bảo trì"]].map(([c,l])=>(
          <div key={l} style={{ display:"flex",alignItems:"center",gap:6,fontSize:12,color:t.muted }}>
            <div style={{ width:12,height:12,borderRadius:3,background:c }}/>
            {l}
          </div>
        ))}
      </div>
      {floors.map(floor=>(
        <div key={floor} style={{ marginBottom:16 }}>
          <div style={{ fontSize:11,fontWeight:700,color:t.muted,letterSpacing:1,marginBottom:8,textTransform:"uppercase" }}>Tầng {floor}</div>
          <div style={{ display:"flex",gap:8,flexWrap:"wrap" }}>
            {rooms.filter(r=>r.floor===floor).map(room=>{
              const bg = statusColor[room.status]?.(room.paid)||"#999";
              return (
                <div key={room.id} onClick={()=>onRoomClick(room)} style={{ width:72,height:64,borderRadius:10,background:bg,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",cursor:"pointer",transition:"transform 0.15s,box-shadow 0.15s",boxShadow:"0 2px 8px rgba(0,0,0,0.2)" }}
                  onMouseEnter={e=>{ e.currentTarget.style.transform="scale(1.08)"; e.currentTarget.style.boxShadow="0 4px 16px rgba(0,0,0,0.35)"; }}
                  onMouseLeave={e=>{ e.currentTarget.style.transform="scale(1)"; e.currentTarget.style.boxShadow="0 2px 8px rgba(0,0,0,0.2)"; }}>
                  <div style={{ fontSize:13,fontWeight:800,color:"#fff" }}>{room.id}</div>
                  {room.tenant&&<div style={{ fontSize:9,color:"rgba(255,255,255,0.8)",marginTop:2,textAlign:"center",padding:"0 4px",overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap",width:64 }}>{room.tenant.split(" ").pop()}</div>}
                </div>
              );
            })}
          </div>
        </div>
      ))}
    </Card>
  );
}

/* ══════════════════════════════════════════════════════════════
   ADMIN DASHBOARD
══════════════════════════════════════════════════════════════ */
function AdminDashboard({ t, dark, setDark, onLogout, user }) {
  const [tab, setTab] = useState("overview");
  const allRooms = INIT_ROOMS;
  const allTickets = INIT_TICKETS;

  const TABS = [
    { id:"overview", icon:"📊", label:"Tổng quan" },
    { id:"buildings", icon:"🏢", label:"Tòa nhà" },
    { id:"users", icon:"👥", label:"Người dùng" },
    { id:"reports", icon:"📈", label:"Báo cáo" },
  ];

  const totalRevenue = allRooms.filter(r=>r.status==="occupied"&&r.paid).reduce((a,r)=>a+calcBill(r).total,0);
  const occupied = allRooms.filter(r=>r.status==="occupied").length;

  return (
    <div style={{ minHeight:"100vh",background:t.bg,fontFamily:"'Segoe UI',system-ui,sans-serif",color:t.text }}>
      <Navbar t={t} dark={dark} setDark={setDark} onLogout={onLogout} user={user} role="admin" title="Admin Panel" subtitle="Quản trị toàn hệ thống"/>
      <div style={{ maxWidth:960,margin:"0 auto",padding:"20px 16px" }}>
        <Tabs tabs={TABS} active={tab} setActive={setTab} t={t}/>
        {tab==="overview"&&<AdminOverview allRooms={allRooms} allTickets={allTickets} totalRevenue={totalRevenue} occupied={occupied} t={t}/>}
        {tab==="buildings"&&<AdminBuildings allRooms={allRooms} t={t}/>}
        {tab==="users"&&<AdminUsers t={t}/>}
        {tab==="reports"&&<AdminReports allRooms={allRooms} t={t}/>}
      </div>
    </div>
  );
}

function AdminOverview({ allRooms, allTickets, totalRevenue, occupied, t }) {
  const byBuilding = ["Sơn Trà","Mỹ An"].map(b=>({
    name:b, total:allRooms.filter(r=>r.building===b).length,
    occupied:allRooms.filter(r=>r.building===b&&r.status==="occupied").length,
    rev:allRooms.filter(r=>r.building===b&&r.status==="occupied"&&r.paid).reduce((a,r)=>a+r.rent,0),
  }));
  return (
    <div>
      <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fit,minmax(140px,1fr))",gap:12,marginBottom:20 }}>
        <StatCard icon="🏢" label="Tổng tòa nhà" value="2" sub="Sơn Trà + Mỹ An" t={t}/>
        <StatCard icon="🏠" label="Tổng phòng" value={allRooms.length} sub={`${occupied} đang thuê`} t={t}/>
        <StatCard icon="💰" label="Doanh thu T6" value={fmtM(totalRevenue)} t={t} accent={t.accent}/>
        <StatCard icon="🔧" label="Ticket chờ" value={allTickets.filter(k=>k.status!=="done").length} t={t} accent={t.orange}/>
        <StatCard icon="👥" label="Người dùng" value={USERS.length} sub="Admin+Host+Tenant" t={t}/>
      </div>
      <Card t={t} p="16px" mb={16}>
        <SectionTitle t={t}>🏢 So sánh theo tòa nhà</SectionTitle>
        <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fit,minmax(240px,1fr))",gap:12 }}>
          {byBuilding.map(b=>(
            <div key={b.name} style={{ padding:"14px 18px",background:t.bg3,borderRadius:12 }}>
              <div style={{ fontSize:15,fontWeight:800,color:t.text,marginBottom:8 }}>🏢 {b.name}</div>
              <div style={{ display:"flex",justifyContent:"space-between",fontSize:13,marginBottom:6 }}>
                <span style={{ color:t.muted }}>Lấp đầy</span>
                <span style={{ fontWeight:700,color:t.green }}>{b.occupied}/{b.total} phòng ({Math.round(b.occupied/b.total*100)}%)</span>
              </div>
              <div style={{ display:"flex",justifyContent:"space-between",fontSize:13 }}>
                <span style={{ color:t.muted }}>Doanh thu T6</span>
                <span style={{ fontWeight:700,color:t.accent }}>{fmtM(b.rev)}</span>
              </div>
            </div>
          ))}
        </div>
      </Card>
      <Card t={t} p="16px">
        <SectionTitle t={t}>📋 Ticket gần đây (tất cả tòa)</SectionTitle>
        {allTickets.slice(0,5).map(k=>(
          <div key={k.id} style={{ display:"flex",alignItems:"center",gap:12,padding:"10px 0",borderBottom:`1px solid ${t.border}` }}>
            <PriorityDot priority={k.priority} t={t}/>
            <div style={{ flex:1,minWidth:0 }}>
              <div style={{ fontSize:13,fontWeight:600,color:t.text,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{k.issue}</div>
              <div style={{ fontSize:11,color:t.muted }}>{k.roomId} · {k.building} · {k.tenant} · {k.date}</div>
            </div>
            <StatusBadge status={k.status} t={t}/>
          </div>
        ))}
      </Card>
    </div>
  );
}

function AdminBuildings({ allRooms, t }) {
  return (
    <div>
      {["Sơn Trà","Mỹ An"].map(building=>{
        const rooms = allRooms.filter(r=>r.building===building);
        const floors = [...new Set(rooms.map(r=>r.floor))].sort();
        return (
          <Card key={building} t={t} p="16px" mb={16}>
            <SectionTitle t={t}>🏢 {building}</SectionTitle>
            {floors.map(floor=>(
              <div key={floor} style={{ marginBottom:12 }}>
                <div style={{ fontSize:11,color:t.muted,fontWeight:700,marginBottom:6 }}>Tầng {floor}</div>
                <div style={{ display:"flex",gap:8,flexWrap:"wrap" }}>
                  {rooms.filter(r=>r.floor===floor).map(room=>(
                    <div key={room.id} style={{ padding:"8px 12px",background:t.bg3,borderRadius:8,fontSize:12,fontWeight:700,color:room.status==="occupied"?t.green:room.status==="maintenance"?t.orange:t.blue }}>
                      {room.id}
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </Card>
        );
      })}
    </div>
  );
}

function AdminUsers({ t }) {
  const roleColor = { admin:t.red, host:t.orange, tenant:t.blue };
  const roleLabel = { admin:"Admin", host:"Chủ trọ", tenant:"Khách thuê" };
  return (
    <Card t={t} p="16px">
      <SectionTitle t={t}>👥 Danh sách người dùng</SectionTitle>
      {USERS.map(u=>(
        <div key={u.id} style={{ display:"flex",alignItems:"center",gap:12,padding:"12px 0",borderBottom:`1px solid ${t.border}` }}>
          <Avatar name={u.name} size={38} bg={roleColor[u.role]+"33"} color={roleColor[u.role]}/>
          <div style={{ flex:1 }}>
            <div style={{ fontSize:13,fontWeight:700,color:t.text }}>{u.name}</div>
            <div style={{ fontSize:11,color:t.muted }}>{u.email}</div>
          </div>
          <Badge label={roleLabel[u.role]} color={roleColor[u.role]} dim={roleColor[u.role]+"22"}/>
        </div>
      ))}
    </Card>
  );
}

function AdminReports({ allRooms, t }) {
  const totalRev = allRooms.filter(r=>r.status==="occupied"&&r.paid).reduce((a,r)=>a+calcBill(r).total,0);
  return (
    <div>
      <Card t={t} p="16px" mb={16}>
        <SectionTitle t={t}>📈 Doanh thu 6 tháng (Tổng hệ thống)</SectionTitle>
        <div style={{ height:220 }}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={REVENUE_DATA} margin={{top:5,right:5,bottom:0,left:-20}}>
              <defs>
                <linearGradient id="grad1" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={t.accent} stopOpacity={0.3}/>
                  <stop offset="95%" stopColor={t.accent} stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke={t.border}/>
              <XAxis dataKey="m" tick={{fill:t.muted,fontSize:11}}/>
              <YAxis tick={{fill:t.muted,fontSize:11}}/>
              <Tooltip contentStyle={{background:t.bg2,border:`1px solid ${t.border}`,borderRadius:8,color:t.text}} formatter={(v,n)=>[v+" tr₫",n==="rev"?"Thực thu":"Mục tiêu"]}/>
              <Legend/>
              <Area type="monotone" dataKey="rev" stroke={t.accent} strokeWidth={2} fill="url(#grad1)" name="Thực thu"/>
              <Area type="monotone" dataKey="target" stroke={t.blue} strokeWidth={1.5} fill="none" strokeDasharray="4 3" name="Mục tiêu"/>
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </Card>
      <Card t={t} p="16px">
        <SectionTitle t={t}>💰 Tổng kết tháng 6/2025</SectionTitle>
        {[
          ["Tổng doanh thu đã thu",fmt(totalRev),t.accent],
          ["Tổng phòng đang thuê",allRooms.filter(r=>r.status==="occupied").length+" phòng",t.green],
          ["Phòng chưa thu tiền",allRooms.filter(r=>r.status==="occupied"&&!r.paid).length+" phòng",t.red],
          ["Tỷ lệ lấp đầy",Math.round(allRooms.filter(r=>r.status==="occupied").length/allRooms.length*100)+"%",t.blue],
        ].map(([k,v,c])=>(
          <div key={k} style={{ display:"flex",justifyContent:"space-between",padding:"10px 0",borderBottom:`1px solid ${t.border}` }}>
            <span style={{ fontSize:13,color:t.muted }}>{k}</span>
            <span style={{ fontSize:14,fontWeight:800,color:c }}>{v}</span>
          </div>
        ))}
      </Card>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   NAVBAR
══════════════════════════════════════════════════════════════ */
function Navbar({ t, dark, setDark, onLogout, user, title, subtitle, notifCount=0 }) {
  const roleColor = { admin:t.red, host:t.accent, tenant:t.blue };
  return (
    <div style={{ background:t.bg2,borderBottom:`1px solid ${t.border}`,padding:"0 20px",display:"flex",alignItems:"center",height:58,position:"sticky",top:0,zIndex:100 }}>
      <div style={{ display:"flex",alignItems:"center",gap:10,flex:1,minWidth:0 }}>
        <div style={{ width:32,height:32,borderRadius:9,background:`linear-gradient(135deg,${t.accent},${t.blue})`,display:"flex",alignItems:"center",justifyContent:"center",fontSize:16,flexShrink:0 }}>🏡</div>
        <div style={{ minWidth:0 }}>
          <div style={{ fontSize:14,fontWeight:900,color:t.text,lineHeight:1.2,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{title}</div>
          <div style={{ fontSize:10,color:t.muted,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{subtitle}</div>
        </div>
      </div>
      <div style={{ display:"flex",gap:8,alignItems:"center",flexShrink:0 }}>
        {notifCount>0&&<div style={{ fontSize:12,fontWeight:700,color:"#fff",background:t.red,borderRadius:"50%",width:20,height:20,display:"flex",alignItems:"center",justifyContent:"center" }}>{notifCount}</div>}
        <div style={{ padding:"4px 10px",borderRadius:20,background:roleColor[user?.role]+"22",fontSize:11,fontWeight:700,color:roleColor[user?.role] }}>{user?.name?.split(" ").pop()}</div>
        <button onClick={()=>setDark(!dark)} style={{ padding:"6px 10px",borderRadius:8,border:`1px solid ${t.border}`,background:t.bg3,color:t.text,cursor:"pointer",fontSize:13 }}>{dark?"☀️":"🌙"}</button>
        <button onClick={onLogout} style={{ padding:"6px 12px",borderRadius:8,border:`1px solid ${t.border}`,background:t.bg3,color:t.muted,cursor:"pointer",fontSize:12 }}>Đăng xuất</button>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   OWNER DASHBOARD
══════════════════════════════════════════════════════════════ */
function OwnerDashboard({ t, dark, setDark, onLogout, user }) {
  const [rooms, setRooms] = useState(INIT_ROOMS.filter(r=>r.hostId===user.id));
  const [tickets, setTickets] = useState(INIT_TICKETS.filter(k=>k.building===user.building));
  const [reviews] = useState(INIT_REVIEWS);
  const [tab, setTab] = useState("overview");

  const TABS = [
    { id:"overview",    icon:"📊", label:"Tổng quan" },
    { id:"floorplan",   icon:"🏢", label:"Sơ đồ tầng" },
    { id:"rooms",       icon:"🏠", label:"Phòng trọ" },
    { id:"billing",     icon:"🧾", label:"Hóa đơn" },
    { id:"maintenance", icon:"🔧", label:"Bảo trì" },
    { id:"inventory",   icon:"📦", label:"Tài sản" },
    { id:"reviews",     icon:"⭐", label:"Đánh giá" },
  ];

  const pendingTickets = tickets.filter(k=>k.status!=="done").length;

  return (
    <div style={{ minHeight:"100vh",background:t.bg,fontFamily:"'Segoe UI',system-ui,sans-serif",color:t.text }}>
      <Navbar t={t} dark={dark} setDark={setDark} onLogout={onLogout} user={user}
        title="The Rental Hub" subtitle={`Chủ trọ · Tòa ${user.building}`} notifCount={pendingTickets}/>
      <div style={{ maxWidth:920,margin:"0 auto",padding:"20px 16px" }}>
        <Tabs tabs={TABS} active={tab} setActive={setTab} t={t}/>
        {tab==="overview"&&<OwnerOverview rooms={rooms} tickets={tickets} t={t}/>}
        {tab==="floorplan"&&<OwnerFloorPlan rooms={rooms} setRooms={setRooms} t={t}/>}
        {tab==="rooms"&&<RoomsTab rooms={rooms} setRooms={setRooms} t={t}/>}
        {tab==="billing"&&<BillingTab rooms={rooms} setRooms={setRooms} t={t}/>}
        {tab==="maintenance"&&<MaintenanceTab tickets={tickets} setTickets={setTickets} t={t}/>}
        {tab==="inventory"&&<InventoryTab rooms={rooms} t={t}/>}
        {tab==="reviews"&&<ReviewsTab reviews={reviews} t={t}/>}
      </div>
    </div>
  );
}

function OwnerOverview({ rooms, tickets, t }) {
  const occupied = rooms.filter(r=>r.status==="occupied").length;
  const unpaid = rooms.filter(r=>r.status==="occupied"&&!r.paid).length;
  const monthRev = rooms.filter(r=>r.status==="occupied"&&r.paid).reduce((a,r)=>a+calcBill(r).total,0);
  const pending = tickets.filter(k=>k.status!=="done").length;
  const occRate = Math.round((occupied/rooms.length)*100);
  const pieData = [
    { name:"Có người", value:occupied, color:t.green },
    { name:"Trống", value:rooms.filter(r=>r.status==="empty").length, color:t.blue },
    { name:"Bảo trì", value:rooms.filter(r=>r.status==="maintenance").length, color:t.orange },
  ];
  return (
    <div>
      <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fit,minmax(130px,1fr))",gap:12,marginBottom:20 }}>
        <StatCard icon="🏠" label="Tổng phòng" value={rooms.length} sub={`${occupied} đang thuê`} t={t}/>
        <StatCard icon="📊" label="Lấp đầy" value={`${occRate}%`} sub="Tháng 6/2025" t={t} accent={t.green}/>
        <StatCard icon="💰" label="Đã thu T6" value={fmtM(monthRev)} sub={`${rooms.filter(r=>r.paid).length} phòng`} t={t} accent={t.accent}/>
        <StatCard icon="⚠️" label="Chưa thu" value={unpaid} sub="Phòng chưa đóng" t={t} accent={t.red}/>
        <StatCard icon="🔧" label="Bảo trì" value={pending} sub="Ticket chờ xử lý" t={t} accent={t.orange}/>
      </div>
      <div style={{ display:"grid",gridTemplateColumns:"1fr 1fr",gap:16,marginBottom:20 }}>
        <Card t={t} p="16px">
          <SectionTitle t={t}>📈 Doanh thu 6 tháng</SectionTitle>
          <div style={{ height:180 }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={REVENUE_DATA} margin={{top:5,right:5,bottom:0,left:-20}}>
                <defs><linearGradient id="rg" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={t.accent} stopOpacity={0.3}/><stop offset="95%" stopColor={t.accent} stopOpacity={0}/></linearGradient></defs>
                <CartesianGrid strokeDasharray="3 3" stroke={t.border}/>
                <XAxis dataKey="m" tick={{fill:t.muted,fontSize:11}}/>
                <YAxis tick={{fill:t.muted,fontSize:11}}/>
                <Tooltip contentStyle={{background:t.bg2,border:`1px solid ${t.border}`,borderRadius:8,color:t.text}} formatter={v=>[v+" tr₫","Doanh thu"]}/>
                <Area type="monotone" dataKey="rev" stroke={t.accent} strokeWidth={2} fill="url(#rg)"/>
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>
        <Card t={t} p="16px">
          <SectionTitle t={t}>🏠 Tình trạng phòng</SectionTitle>
          <div style={{ display:"flex",alignItems:"center",gap:12 }}>
            <div style={{ height:160,width:130,flexShrink:0 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={42} outerRadius={60} dataKey="value" paddingAngle={3}>
                    {pieData.map((d,i)=><Cell key={i} fill={d.color}/>)}
                  </Pie>
                  <Tooltip contentStyle={{background:t.bg2,border:`1px solid ${t.border}`,borderRadius:8}}/>
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div style={{ flex:1 }}>
              {pieData.map((d,i)=>(
                <div key={i} style={{ display:"flex",alignItems:"center",gap:8,marginBottom:12 }}>
                  <div style={{ width:10,height:10,borderRadius:3,background:d.color,flexShrink:0 }}/>
                  <span style={{ fontSize:12,color:t.muted }}>{d.name}</span>
                  <span style={{ fontSize:18,fontWeight:800,color:d.color,marginLeft:"auto" }}>{d.value}</span>
                </div>
              ))}
            </div>
          </div>
        </Card>
      </div>
      <Card t={t} p="16px">
        <SectionTitle t={t}>⚡ Ticket gần đây</SectionTitle>
        {tickets.slice(0,4).map(k=>(
          <div key={k.id} style={{ display:"flex",alignItems:"center",gap:12,padding:"10px 0",borderBottom:`1px solid ${t.border}` }}>
            <PriorityDot priority={k.priority} t={t}/>
            <div style={{ flex:1,minWidth:0 }}>
              <div style={{ fontSize:13,fontWeight:600,color:t.text,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{k.issue}</div>
              <div style={{ fontSize:11,color:t.muted }}>{k.roomId} · {k.tenant} · {k.date}</div>
            </div>
            <StatusBadge status={k.status} t={t}/>
          </div>
        ))}
      </Card>
    </div>
  );
}

function OwnerFloorPlan({ rooms, setRooms, t }) {
  const [selRoom, setSelRoom] = useState(null);
  const [showInvoice, setShowInvoice] = useState(false);
  const [showInventory, setShowInventory] = useState(false);
  return (
    <div>
      <FloorPlan rooms={rooms} t={t} onRoomClick={r=>setSelRoom(r)}/>
      {selRoom&&(
        <Card t={t} p="20px">
          <div style={{ display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:16 }}>
            <h3 style={{ margin:0,color:t.text,fontSize:16,fontWeight:800 }}>📋 Phòng {selRoom.id}</h3>
            <Btn variant="ghost" t={t} small onClick={()=>setSelRoom(null)}>✕</Btn>
          </div>
          <div style={{ display:"grid",gridTemplateColumns:"1fr 1fr",gap:12,marginBottom:16 }}>
            {[
              ["Người thuê",selRoom.tenant||"—"],
              ["SĐT",selRoom.phone||"—"],
              ["Tiền phòng",fmt(selRoom.rent)],
              ["Trạng thái",""],
            ].map(([k,v],i)=>(
              <div key={k}>
                <div style={{ fontSize:11,color:t.muted,marginBottom:3 }}>{k}</div>
                {i===3?<StatusBadge status={selRoom.status} t={t}/>:<div style={{ fontSize:14,fontWeight:600,color:t.text }}>{v}</div>}
              </div>
            ))}
          </div>
          {selRoom.status==="occupied"&&(
            <div style={{ display:"flex",gap:8,flexWrap:"wrap" }}>
              <Btn t={t} small onClick={()=>setShowInvoice(true)}>🧾 Xem hóa đơn</Btn>
              <Btn t={t} variant="secondary" small onClick={()=>setShowInventory(true)}>📦 Tài sản</Btn>
              <Btn t={t} variant={selRoom.paid?"danger":"success"} small onClick={()=>{setSelRoom(s=>({...s,paid:!s.paid}));setRooms(rs=>rs.map(r=>r.id===selRoom.id?{...r,paid:!r.paid}:r));}}>
                {selRoom.paid?"✗ Hủy thu":"✓ Xác nhận thu tiền"}
              </Btn>
            </div>
          )}
        </Card>
      )}
      <InvoiceModal room={selRoom} open={showInvoice} onClose={()=>setShowInvoice(false)} t={t}/>
      <InventoryModal roomId={selRoom?.id} open={showInventory} onClose={()=>setShowInventory(false)} t={t}/>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   BILLING TAB
══════════════════════════════════════════════════════════════ */
function BillingTab({ rooms, setRooms, t }) {
  const [invoiceRoom, setInvoiceRoom] = useState(null);
  const [showInvoice, setShowInvoice] = useState(false);
  const [electricEdit, setElectricEdit] = useState({});
  const [editingRoom, setEditingRoom] = useState(null);

  const occupied = rooms.filter(r=>r.status==="occupied");
  const totalCollected = occupied.filter(r=>r.paid).reduce((a,r)=>a+calcBill(r).total,0);
  const totalPending = occupied.filter(r=>!r.paid).reduce((a,r)=>a+calcBill(r).total,0);

  const saveElectric = (roomId) => {
    if (!electricEdit[roomId]) return;
    setRooms(rs=>rs.map(r=>r.id===roomId?{...r,electricNew:parseInt(electricEdit[roomId])||r.electricNew}:r));
    setEditingRoom(null);
  };

  return (
    <div>
      <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fit,minmax(160px,1fr))",gap:12,marginBottom:20 }}>
        <StatCard icon="✅" label="Đã thu" value={fmtM(totalCollected)} sub={`${occupied.filter(r=>r.paid).length} phòng`} t={t} accent={t.green}/>
        <StatCard icon="⏳" label="Chưa thu" value={fmtM(totalPending)} sub={`${occupied.filter(r=>!r.paid).length} phòng`} t={t} accent={t.red}/>
        <StatCard icon="📊" label="Tổng T6" value={fmtM(totalCollected+totalPending)} t={t} accent={t.accent}/>
      </div>

      <Card t={t} p="16px" mb={16}>
        <SectionTitle t={t}>⚡ Cập nhật chỉ số điện</SectionTitle>
        <div style={{ fontSize:12,color:t.muted,marginBottom:12,padding:"8px 12px",background:t.bg3,borderRadius:8 }}>
          💡 Hệ thống tự chốt sổ ngày 30 hàng tháng và gửi email + mã QR đến từng khách
        </div>
        <div style={{ overflowX:"auto" }}>
          <table style={{ width:"100%",borderCollapse:"collapse",fontSize:13 }}>
            <thead>
              <tr style={{ color:t.muted,fontSize:11 }}>
                {["Phòng","Người thuê","Cũ (kWh)","Mới (kWh)","Tiêu thụ","Tiền điện",""].map(h=>(
                  <th key={h} style={{ padding:"6px 10px",textAlign:"left",fontWeight:700,borderBottom:`1px solid ${t.border}` }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {occupied.map(room=>{
                const kWh = room.electricNew-room.electricOld;
                const bill = kWh*ELEC_PRICE;
                const isEditing = editingRoom===room.id;
                return (
                  <tr key={room.id} style={{ borderBottom:`1px solid ${t.border}` }}>
                    <td style={{ padding:"8px 10px",fontWeight:700,color:t.text }}>{room.id}</td>
                    <td style={{ padding:"8px 10px",color:t.muted,maxWidth:120,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{room.tenant}</td>
                    <td style={{ padding:"8px 10px",color:t.muted }}>{room.electricOld}</td>
                    <td style={{ padding:"8px 10px" }}>
                      {isEditing
                        ? <input type="number" defaultValue={room.electricNew} onChange={e=>setElectricEdit(x=>({...x,[room.id]:e.target.value}))} style={{ width:70,padding:"4px 8px",borderRadius:6,border:`1px solid ${t.border}`,background:t.bg3,color:t.text,fontSize:12 }}/>
                        : <span style={{ color:t.text }}>{room.electricNew}</span>
                      }
                    </td>
                    <td style={{ padding:"8px 10px",color:t.blue,fontWeight:600 }}>{kWh} kWh</td>
                    <td style={{ padding:"8px 10px",color:t.accent,fontWeight:700 }}>{fmtM(bill)}</td>
                    <td style={{ padding:"8px 10px" }}>
                      {isEditing
                        ? <div style={{ display:"flex",gap:4 }}>
                            <Btn t={t} small onClick={()=>saveElectric(room.id)}>💾</Btn>
                            <Btn t={t} small variant="ghost" onClick={()=>setEditingRoom(null)}>✕</Btn>
                          </div>
                        : <Btn t={t} small variant="ghost" onClick={()=>setEditingRoom(room.id)}>✏️ Sửa</Btn>
                      }
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </Card>

      <Card t={t} p="16px">
        <SectionTitle t={t}>🧾 Hóa đơn tháng 6/2025</SectionTitle>
        {occupied.map(room=>{
          const bill = calcBill(room);
          return (
            <div key={room.id} style={{ display:"flex",alignItems:"center",gap:12,padding:"12px 0",borderBottom:`1px solid ${t.border}` }}>
              <div style={{ width:40,height:40,borderRadius:10,background:room.paid?t.greenDim:t.redDim,display:"flex",alignItems:"center",justifyContent:"center",fontSize:18,flexShrink:0 }}>
                {room.paid?"✅":"⏳"}
              </div>
              <div style={{ flex:1,minWidth:0 }}>
                <div style={{ fontSize:13,fontWeight:700,color:t.text }}>{room.id} — {room.tenant}</div>
                <div style={{ fontSize:12,color:t.muted,marginTop:2 }}>
                  Phòng: {fmtM(bill.rent)} · Điện: {fmtM(bill.electric)} · Nước: {fmtM(bill.water)} · DV: {fmtM(bill.service)}
                </div>
              </div>
              <div style={{ textAlign:"right",flexShrink:0 }}>
                <div style={{ fontSize:15,fontWeight:800,color:room.paid?t.green:t.red }}>{fmt(bill.total)}</div>
                <div style={{ marginTop:4,display:"flex",gap:6 }}>
                  <Btn t={t} small onClick={()=>{setInvoiceRoom(room);setShowInvoice(true);}}>🧾 Xem</Btn>
                  <Btn t={t} small variant={room.paid?"danger":"success"}
                    onClick={()=>setRooms(rs=>rs.map(r=>r.id===room.id?{...r,paid:!r.paid}:r))}>
                    {room.paid?"Hủy thu":"Thu tiền"}
                  </Btn>
                </div>
              </div>
            </div>
          );
        })}
      </Card>
      <InvoiceModal room={invoiceRoom} open={showInvoice} onClose={()=>setShowInvoice(false)} t={t}/>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   ROOMS TAB
══════════════════════════════════════════════════════════════ */
function RoomsTab({ rooms, setRooms, t }) {
  const [sel, setSel] = useState(null);
  const [showInvoice, setShowInvoice] = useState(false);
  const [showInventory, setShowInventory] = useState(false);
  const [filterStatus, setFilterStatus] = useState("all");

  const filtered = filterStatus==="all"?rooms:rooms.filter(r=>r.status===filterStatus);
  const floors = [...new Set(filtered.map(r=>r.floor))].sort();

  return (
    <div>
      <div style={{ display:"flex",gap:8,marginBottom:16,flexWrap:"wrap" }}>
        {[["all","Tất cả"],["occupied","Có người"],["empty","Trống"],["maintenance","Bảo trì"]].map(([v,l])=>(
          <button key={v} onClick={()=>setFilterStatus(v)} style={{ padding:"6px 14px",borderRadius:20,border:`1px solid ${filterStatus===v?t.accent:t.border}`,background:filterStatus===v?t.accentDim:"transparent",color:filterStatus===v?t.accent:t.muted,fontSize:12,fontWeight:filterStatus===v?700:500,cursor:"pointer" }}>
            {l}
          </button>
        ))}
      </div>

      {floors.map(floor=>(
        <div key={floor} style={{ marginBottom:20 }}>
          <div style={{ fontSize:12,fontWeight:700,color:t.muted,letterSpacing:1,marginBottom:10,textTransform:"uppercase" }}>Tầng {floor}</div>
          <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fill,minmax(170px,1fr))",gap:10 }}>
            {filtered.filter(r=>r.floor===floor).map(room=>(
              <div key={room.id} onClick={()=>setSel(sel?.id===room.id?null:room)}
                style={{ background:t.bg2,border:`2px solid ${sel?.id===room.id?t.accent:t.border}`,borderRadius:12,padding:"14px 16px",cursor:"pointer",transition:"all 0.15s",borderLeft:`4px solid ${room.status==="occupied"?(room.paid?t.green:t.red):room.status==="maintenance"?t.orange:t.blue}` }}>
                <div style={{ display:"flex",justifyContent:"space-between",alignItems:"flex-start" }}>
                  <div style={{ fontSize:18,fontWeight:800,color:t.text }}>{room.id}</div>
                  <StatusBadge status={room.status} t={t}/>
                </div>
                {room.tenant&&<div style={{ fontSize:12,color:t.muted,marginTop:6,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{room.tenant}</div>}
                <div style={{ fontSize:13,fontWeight:700,color:t.accent,marginTop:4 }}>{fmtM(room.rent)}/tháng</div>
                {room.status==="occupied"&&(
                  <div style={{ marginTop:8,fontSize:11,color:room.paid?t.green:t.red,fontWeight:700 }}>
                    {room.paid?"✓ Đã thanh toán":"✗ Chưa thanh toán"}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      ))}

      {sel&&(
        <Card t={t} p="20px">
          <div style={{ display:"flex",justifyContent:"space-between",alignItems:"center",marginBottom:16 }}>
            <h3 style={{ margin:0,color:t.text,fontSize:16,fontWeight:800 }}>Chi tiết — Phòng {sel.id}</h3>
            <Btn variant="ghost" t={t} small onClick={()=>setSel(null)}>✕</Btn>
          </div>
          <div style={{ display:"grid",gridTemplateColumns:"1fr 1fr",gap:14,marginBottom:16 }}>
            {[
              ["Người thuê",sel.tenant||"—"],
              ["SĐT",sel.phone||"—"],
              ["Ngày vào",sel.moveIn||"—"],
              ["Tiền phòng",fmt(sel.rent)],
              ["Tiền cọc",sel.deposit?fmt(sel.deposit):"—"],
              ["Tiêu thụ điện",`${sel.electricNew-sel.electricOld} kWh`],
              ["Tiền điện",fmt((sel.electricNew-sel.electricOld)*ELEC_PRICE)],
              ["Tiền nước",fmt(sel.water*WATER_PRICE)],
              ["Phí dịch vụ",fmt(sel.serviceFee)],
              ["Tổng T6",fmt(calcBill(sel).total)],
            ].map(([k,v])=>(
              <div key={k}>
                <div style={{ fontSize:11,color:t.muted,marginBottom:3 }}>{k}</div>
                <div style={{ fontSize:13,fontWeight:700,color:k==="Tổng T6"?t.accent:t.text }}>{v}</div>
              </div>
            ))}
          </div>
          {sel.status==="occupied"&&(
            <div style={{ display:"flex",gap:8,flexWrap:"wrap",paddingTop:14,borderTop:`1px solid ${t.border}` }}>
              <Btn t={t} small onClick={()=>setShowInvoice(true)}>🧾 Hóa đơn</Btn>
              <Btn t={t} variant="secondary" small onClick={()=>setShowInventory(true)}>📦 Tài sản phòng</Btn>
              <Btn t={t} variant={sel.paid?"danger":"success"} small onClick={()=>{setRooms(rs=>rs.map(r=>r.id===sel.id?{...r,paid:!r.paid}:r));setSel(s=>({...s,paid:!s.paid}));}}>
                {sel.paid?"✗ Hủy thu":"✓ Xác nhận đã thu"}
              </Btn>
            </div>
          )}
        </Card>
      )}
      <InvoiceModal room={sel} open={showInvoice} onClose={()=>setShowInvoice(false)} t={t}/>
      <InventoryModal roomId={sel?.id} open={showInventory} onClose={()=>setShowInventory(false)} t={t}/>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   MAINTENANCE TAB (Owner)
══════════════════════════════════════════════════════════════ */
function MaintenanceTab({ tickets, setTickets, t }) {
  const [sel, setSel] = useState(null);
  const [costInput, setCostInput] = useState("");
  const [noteInput, setNoteInput] = useState("");
  const next = { pending:"in_progress", in_progress:"done" };
  const nextLabel = { pending:"▶ Bắt đầu xử lý", in_progress:"✅ Hoàn thành" };

  const advance = (ticket) => {
    const newStatus = next[ticket.status];
    const action = newStatus==="in_progress"?"Chủ trọ bắt đầu xử lý"
      : `Hoàn thành${costInput?`. Chi phí: ${parseInt(costInput).toLocaleString("vi-VN")}₫`:""}`;
    setTickets(ts=>ts.map(k=>k.id===ticket.id?{
      ...k, status:newStatus,
      repairCost:newStatus==="done"&&costInput?parseInt(costInput):k.repairCost,
      history:[...k.history,{date:new Date().toLocaleDateString("vi-VN"),action,by:"Chủ trọ"}],
    }:k));
    setCostInput(""); setNoteInput("");
    if(sel?.id===ticket.id) setSel(prev=>({...prev,status:newStatus,history:[...prev.history,{date:new Date().toLocaleDateString("vi-VN"),action,by:"Chủ trọ"}]}));
  };

  const totalRepairCost = tickets.filter(k=>k.repairCost).reduce((a,k)=>a+(k.repairCost||0),0);

  return (
    <div>
      <div style={{ display:"grid",gridTemplateColumns:"repeat(3,1fr)",gap:10,marginBottom:20 }}>
        {[["pending","⏳ Chờ xử lý",t.orangeDim,t.orange],["in_progress","🔧 Đang sửa",t.blueDim,t.blue],["done","✅ Hoàn thành",t.greenDim,t.green]].map(([s,label,bg,color])=>(
          <div key={s} style={{ background:bg,borderRadius:12,padding:"14px 16px",textAlign:"center" }}>
            <div style={{ fontSize:24,fontWeight:900,color }}>{tickets.filter(k=>k.status===s).length}</div>
            <div style={{ fontSize:12,color,fontWeight:700,marginTop:2 }}>{label}</div>
          </div>
        ))}
      </div>

      {totalRepairCost>0&&(
        <div style={{ padding:"10px 16px",borderRadius:10,background:t.orangeDim,border:`1px solid ${t.orange}22`,marginBottom:16,fontSize:13,color:t.orange,fontWeight:600 }}>
          🔧 Chi phí sửa chữa tháng 6: {fmt(totalRepairCost)}
        </div>
      )}

      {tickets.map(k=>(
        <Card key={k.id} t={t} p="16px" mb={10}>
          <div style={{ display:"flex",gap:12,alignItems:"flex-start" }}>
            <div style={{ width:38,height:38,borderRadius:10,background:k.priority==="high"?t.redDim:k.priority==="medium"?t.orangeDim:t.bg3,display:"flex",alignItems:"center",justifyContent:"center",fontSize:20,flexShrink:0 }}>
              {k.cat==="Nước"?"💧":k.cat==="Điện"||k.cat==="Điện lạnh"?"⚡":"🔧"}
            </div>
            <div style={{ flex:1,minWidth:0 }}>
              <div style={{ display:"flex",gap:8,alignItems:"center",flexWrap:"wrap",marginBottom:4 }}>
                <span style={{ fontSize:13,fontWeight:800,color:t.text }}>{k.issue}</span>
                <PriorityDot priority={k.priority} t={t}/>
                <Badge label={k.cat} color={t.muted} dim={t.bg3} small/>
              </div>
              <div style={{ fontSize:12,color:t.muted }}>Phòng {k.roomId} · {k.tenant} · {k.date}</div>
              {k.desc&&<div style={{ fontSize:12,color:t.muted,marginTop:4,fontStyle:"italic" }}>"{k.desc}"</div>}
              {k.repairCost&&<div style={{ fontSize:12,color:t.orange,fontWeight:700,marginTop:4 }}>Chi phí: {fmt(k.repairCost)}</div>}
            </div>
            <div style={{ display:"flex",flexDirection:"column",gap:6,alignItems:"flex-end",flexShrink:0 }}>
              <StatusBadge status={k.status} t={t}/>
              <Btn t={t} variant="ghost" small onClick={()=>setSel(sel?.id===k.id?null:k)}>
                {sel?.id===k.id?"▲ Thu gọn":"▼ Chi tiết"}
              </Btn>
              {k.status!=="done"&&(
                <Btn t={t} small variant={k.status==="pending"?"secondary":"primary"} onClick={()=>advance(k)}>
                  {nextLabel[k.status]}
                </Btn>
              )}
            </div>
          </div>
          {sel?.id===k.id&&(
            <div style={{ marginTop:14,paddingTop:14,borderTop:`1px solid ${t.border}` }}>
              <div style={{ fontSize:12,fontWeight:700,color:t.muted,marginBottom:8 }}>📋 Lịch sử xử lý</div>
              {k.history.map((h,i)=>(
                <div key={i} style={{ display:"flex",gap:10,padding:"6px 0",borderBottom:`1px dashed ${t.border}` }}>
                  <div style={{ fontSize:11,color:t.muted,minWidth:80 }}>{h.date}</div>
                  <div style={{ fontSize:12,color:t.text,flex:1 }}>{h.action}</div>
                  <div style={{ fontSize:11,color:t.muted }}>{h.by}</div>
                </div>
              ))}
              {k.status==="in_progress"&&(
                <div style={{ marginTop:12,display:"flex",gap:8 }}>
                  <input type="number" placeholder="Chi phí sửa chữa (₫)" value={costInput} onChange={e=>setCostInput(e.target.value)}
                    style={{ flex:1,padding:"7px 12px",borderRadius:8,border:`1px solid ${t.border}`,background:t.bg3,color:t.text,fontSize:12 }}/>
                  <Btn t={t} small variant="success" onClick={()=>advance(k)}>✅ Hoàn thành</Btn>
                </div>
              )}
            </div>
          )}
        </Card>
      ))}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   INVENTORY TAB
══════════════════════════════════════════════════════════════ */
function InventoryTab({ rooms, t }) {
  const [selRoom, setSelRoom] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const occupied = rooms.filter(r=>r.status==="occupied");
  const condColor = { "Tốt":t.green, "Bình thường":t.orange, "Hỏng":t.red };

  return (
    <div>
      <Card t={t} p="16px" mb={16}>
        <SectionTitle t={t}>📦 Quản lý tài sản theo phòng</SectionTitle>
        <div style={{ fontSize:12,color:t.muted,marginBottom:12,padding:"8px 12px",background:t.bg3,borderRadius:8 }}>
          💡 Khi khách trả phòng, xuất checklist để kiểm kê tài sản. Click vào phòng để xem chi tiết.
        </div>
        <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fill,minmax(150px,1fr))",gap:10 }}>
          {occupied.map(room=>{
            const items = INIT_INVENTORY[room.id]||[];
            const damaged = items.filter(i=>i.condition==="Hỏng").length;
            return (
              <div key={room.id} onClick={()=>{setSelRoom(room);setShowModal(true);}}
                style={{ padding:"14px",background:t.bg3,borderRadius:12,cursor:"pointer",border:`1px solid ${damaged>0?t.red+"44":t.border}`,transition:"all 0.15s" }}
                onMouseEnter={e=>e.currentTarget.style.borderColor=t.accent}
                onMouseLeave={e=>e.currentTarget.style.borderColor=damaged>0?t.red+"44":t.border}>
                <div style={{ fontSize:15,fontWeight:800,color:t.text }}>{room.id}</div>
                <div style={{ fontSize:11,color:t.muted,marginTop:2,overflow:"hidden",textOverflow:"ellipsis",whiteSpace:"nowrap" }}>{room.tenant||"—"}</div>
                <div style={{ marginTop:8,fontSize:12 }}>
                  <span style={{ color:t.muted }}>{items.length} món đồ</span>
                  {damaged>0&&<span style={{ color:t.red,fontWeight:700,marginLeft:8 }}>⚠️ {damaged} hỏng</span>}
                </div>
              </div>
            );
          })}
        </div>
      </Card>
      <InventoryModal roomId={selRoom?.id} open={showModal} onClose={()=>setShowModal(false)} t={t}/>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   REVIEWS TAB
══════════════════════════════════════════════════════════════ */
function ReviewsTab({ reviews, t }) {
  const avg = (field) => (reviews.reduce((a,r)=>a+r[field],0)/reviews.length).toFixed(1);
  const barData = REVENUE_DATA.map(d=>({ m:d.m, occ:d.occ }));
  return (
    <div>
      <Card t={t} p="16px" mb={16}>
        <SectionTitle t={t}>⭐ Đánh giá tổng quan</SectionTitle>
        <div style={{ display:"grid",gridTemplateColumns:"repeat(auto-fit,minmax(100px,1fr))",gap:10,marginBottom:16 }}>
          {[["rating","Tổng thể","⭐"],["sec","An ninh","🔒"],["util","Điện nước","💡"],["land","Chủ trọ","🏠"]].map(([f,label,ico])=>(
            <div key={f} style={{ textAlign:"center",padding:"14px 8px",background:t.bg3,borderRadius:12 }}>
              <div style={{ fontSize:22 }}>{ico}</div>
              <div style={{ fontSize:24,fontWeight:900,color:t.accent,marginTop:4 }}>{avg(f)}</div>
              <div style={{ fontSize:11,color:t.muted,marginTop:2 }}>{label}</div>
              <Stars v={Math.round(parseFloat(avg(f)))} size={12}/>
            </div>
          ))}
        </div>
        <div style={{ height:150 }}>
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={barData} margin={{top:0,right:0,bottom:0,left:-20}}>
              <CartesianGrid strokeDasharray="3 3" stroke={t.border}/>
              <XAxis dataKey="m" tick={{fill:t.muted,fontSize:11}}/>
              <YAxis tick={{fill:t.muted,fontSize:11}}/>
              <Tooltip contentStyle={{background:t.bg2,border:`1px solid ${t.border}`,borderRadius:8}} formatter={v=>[v+"%","Lấp đầy"]}/>
              <Bar dataKey="occ" fill={t.accent} radius={[4,4,0,0]}/>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>
      {reviews.map(r=>(
        <Card key={r.id} t={t} p="16px" mb={10}>
          <div style={{ display:"flex",gap:12,alignItems:"flex-start" }}>
            <Avatar name={r.tenant} size={40} bg={t.accentDim} color={t.accent}/>
            <div style={{ flex:1 }}>
              <div style={{ display:"flex",justifyContent:"space-between",alignItems:"center" }}>
                <span style={{ fontSize:14,fontWeight:800,color:t.text }}>{r.tenant}</span>
                <span style={{ fontSize:11,color:t.muted }}>{r.date}</span>
              </div>
              <Stars v={r.rating}/>
              <p style={{ margin:"8px 0",fontSize:13,color:t.muted,lineHeight:1.6 }}>{r.comment}</p>
              <div style={{ display:"flex",gap:14,flexWrap:"wrap" }}>
                {[["An ninh",r.sec],["Điện nước",r.util],["Chủ trọ",r.land]].map(([l,v])=>(
                  <div key={l} style={{ display:"flex",alignItems:"center",gap:4,fontSize:12,color:t.muted }}>
                    <span>{l}:</span><Stars v={v} size={11}/>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   TENANT PORTAL
══════════════════════════════════════════════════════════════ */
function TenantPortal({ t, dark, setDark, onLogout, user }) {
  const myRoom = INIT_ROOMS.find(r=>r.id===user.roomId)||INIT_ROOMS[1];
  const [tab, setTab] = useState("room");
  const [myTickets, setMyTickets] = useState(INIT_TICKETS.filter(k=>k.roomId===user.roomId));
  const [showInvoice, setShowInvoice] = useState(false);

  const TABS = [
    { id:"room", icon:"🏠", label:"Phòng tôi" },
    { id:"ticket", icon:"🔧", label:"Báo hỏng" },
    { id:"invoice", icon:"🧾", label:"Hóa đơn" },
  ];

  return (
    <div style={{ minHeight:"100vh",background:t.bg,fontFamily:"'Segoe UI',system-ui,sans-serif",color:t.text }}>
      <Navbar t={t} dark={dark} setDark={setDark} onLogout={onLogout} user={user}
        title={`Phòng ${myRoom.id}`} subtitle={`Khách thuê · Tầng ${myRoom.floor}`} notifCount={myTickets.filter(k=>k.status!=="done").length}/>
      <div style={{ maxWidth:680,margin:"0 auto",padding:"20px 16px" }}>
        <Tabs tabs={TABS} active={tab} setActive={setTab} t={t}/>
        {tab==="room"&&<TenantRoomTab room={myRoom} t={t} onViewInvoice={()=>setShowInvoice(true)}/>}
        {tab==="ticket"&&<TenantTicketTab myRoom={myRoom} user={user} tickets={myTickets} setTickets={setMyTickets} t={t}/>}
        {tab==="invoice"&&<TenantInvoiceTab room={myRoom} t={t}/>}
      </div>
      <InvoiceModal room={myRoom} open={showInvoice} onClose={()=>setShowInvoice(false)} t={t}/>
    </div>
  );
}

function TenantRoomTab({ room, t, onViewInvoice }) {
  const bill = calcBill(room);
  return (
    <div>
      <Card t={t} p="20px" mb={16}>
        <div style={{ display:"flex",justifyContent:"space-between",alignItems:"flex-start",marginBottom:16 }}>
          <div>
            <div style={{ fontSize:32,fontWeight:900,color:t.accent,letterSpacing:-1 }}>{room.id}</div>
            <div style={{ fontSize:13,color:t.muted }}>Tòa nhà {room.building} · Tầng {room.floor}</div>
            {room.moveIn&&<div style={{ fontSize:12,color:t.muted,marginTop:2 }}>Ngày vào: {room.moveIn}</div>}
          </div>
          <QRCode value={`phong-${room.id}-t6-2025-${bill.total}`} size={90}/>
        </div>
        <div style={{ padding:"10px 14px",background:t.blueDim,borderRadius:10,fontSize:12,color:t.blue,marginBottom:16 }}>
          📱 Quét mã QR để thanh toán hoặc xem hóa đơn chi tiết
        </div>
        <div style={{ padding:"12px 16px",borderRadius:10,background:room.paid?t.greenDim:t.redDim,fontSize:13,fontWeight:700,color:room.paid?t.green:t.red,textAlign:"center",marginBottom:16 }}>
          {room.paid?"✅ Đã thanh toán tháng 6/2025":"⚠️ Chưa thanh toán — Hạn: 15/06/2025"}
        </div>
        <Btn t={t} full onClick={onViewInvoice}>🧾 Xem hóa đơn chi tiết</Btn>
      </Card>

      <Card t={t} p="16px" mb={16}>
        <SectionTitle t={t}>💡 Chỉ số tiêu thụ T6</SectionTitle>
        <div style={{ display:"grid",gridTemplateColumns:"1fr 1fr",gap:12 }}>
          <div style={{ padding:"14px",background:t.orangeDim,borderRadius:12 }}>
            <div style={{ fontSize:22 }}>⚡</div>
            <div style={{ fontSize:22,fontWeight:900,color:t.orange }}>{room.electricNew-room.electricOld} kWh</div>
            <div style={{ fontSize:12,color:t.muted }}>Điện tháng 6</div>
            <div style={{ fontSize:13,fontWeight:700,color:t.orange,marginTop:4 }}>{fmt(bill.electric)}</div>
          </div>
          <div style={{ padding:"14px",background:t.blueDim,borderRadius:12 }}>
            <div style={{ fontSize:22 }}>💧</div>
            <div style={{ fontSize:22,fontWeight:900,color:t.blue }}>{room.water} m³</div>
            <div style={{ fontSize:12,color:t.muted }}>Nước tháng 6</div>
            <div style={{ fontSize:13,fontWeight:700,color:t.blue,marginTop:4 }}>{fmt(bill.water)}</div>
          </div>
        </div>
      </Card>

      <Card t={t} p="16px">
        <SectionTitle t={t}>📍 Liên hệ chủ trọ</SectionTitle>
        {[["📞 Điện thoại","090 123 4567"],["🏠 Địa chỉ","123 Hoàng Diệu, Sơn Trà, Đà Nẵng"],["📧 Email","contact@rentalhub.vn"],["🕐 Giờ hỗ trợ","8:00 – 21:00 hàng ngày"]].map(([k,v])=>(
          <div key={k} style={{ display:"flex",gap:12,fontSize:13,padding:"8px 0",borderBottom:`1px solid ${t.border}` }}>
            <span style={{ color:t.muted,minWidth:120 }}>{k}</span>
            <span style={{ color:t.text,fontWeight:600 }}>{v}</span>
          </div>
        ))}
      </Card>
    </div>
  );
}

function TenantTicketTab({ myRoom, user, tickets, setTickets, t }) {
  const [form, setForm] = useState({ issue:"", cat:"Điện", desc:"", priority:"medium" });
  const [sent, setSent] = useState(false);

  const submit = () => {
    if (!form.issue.trim()) return;
    const newT = {
      id:"TK"+String(Date.now()).slice(-4),
      roomId:myRoom.id, building:myRoom.building,
      tenant:myRoom.tenant, issue:form.issue,
      cat:form.cat, status:"pending",
      date:new Date().toLocaleDateString("vi-VN"),
      priority:form.priority, desc:form.desc,
      history:[{date:new Date().toLocaleDateString("vi-VN"),action:"Tạo ticket",by:myRoom.tenant}],
    };
    setTickets(ts=>[newT,...ts]);
    setForm({ issue:"", cat:"Điện", desc:"", priority:"medium" });
    setSent(true);
    setTimeout(()=>setSent(false),3500);
  };

  return (
    <div>
      {sent&&(
        <div style={{ padding:"12px 16px",borderRadius:12,background:t.greenDim,border:`1px solid ${t.green}44`,marginBottom:16,fontSize:14,fontWeight:700,color:t.green,textAlign:"center" }}>
          ✅ Yêu cầu đã gửi thành công! Chủ trọ sẽ phản hồi sớm nhất có thể.
        </div>
      )}
      <Card t={t} p="20px" mb={20}>
        <SectionTitle t={t}>🔧 Gửi yêu cầu bảo trì</SectionTitle>
        <div style={{ display:"flex",flexDirection:"column",gap:12 }}>
          <div>
            <label style={{ fontSize:12,color:t.muted,display:"block",marginBottom:5,fontWeight:600 }}>Danh mục sự cố</label>
            <Select value={form.cat} onChange={e=>setForm(f=>({...f,cat:e.target.value}))} t={t}
              options={["Điện","Nước","Điện lạnh","Cơ sở vật chất","Khác"].map(v=>({v,l:v}))}/>
          </div>
          <div>
            <label style={{ fontSize:12,color:t.muted,display:"block",marginBottom:5,fontWeight:600 }}>Mức độ ưu tiên</label>
            <div style={{ display:"flex",gap:8 }}>
              {[["low","Thấp",t.muted],["medium","Trung bình",t.orange],["high","Khẩn cấp",t.red]].map(([v,l,c])=>(
                <button key={v} onClick={()=>setForm(f=>({...f,priority:v}))} style={{ flex:1,padding:"7px",borderRadius:8,border:`2px solid ${form.priority===v?c:t.border}`,background:form.priority===v?c+"22":"transparent",color:form.priority===v?c:t.muted,fontSize:12,fontWeight:form.priority===v?700:500,cursor:"pointer" }}>{l}</button>
              ))}
            </div>
          </div>
          <div>
            <label style={{ fontSize:12,color:t.muted,display:"block",marginBottom:5,fontWeight:600 }}>Mô tả vấn đề *</label>
            <Input value={form.issue} onChange={e=>setForm(f=>({...f,issue:e.target.value}))} placeholder="VD: Điều hòa không hoạt động, có tiếng kêu..." t={t}/>
          </div>
          <div>
            <label style={{ fontSize:12,color:t.muted,display:"block",marginBottom:5,fontWeight:600 }}>Chi tiết thêm (tuỳ chọn)</label>
            <Input value={form.desc} onChange={e=>setForm(f=>({...f,desc:e.target.value}))} placeholder="Mô tả chi tiết để chủ trọ xử lý nhanh hơn..." t={t} as="textarea"/>
          </div>
          <Btn t={t} onClick={submit} disabled={!form.issue.trim()} full>📤 Gửi yêu cầu sửa chữa</Btn>
        </div>
      </Card>

      <SectionTitle t={t}>📋 Yêu cầu của tôi ({tickets.length})</SectionTitle>
      {tickets.length===0
        ? <div style={{ textAlign:"center",color:t.muted,padding:"40px 0",fontSize:13 }}>Chưa có yêu cầu nào.</div>
        : tickets.map(k=>(
          <Card key={k.id} t={t} p="16px" mb={10}>
            <div style={{ display:"flex",justifyContent:"space-between",alignItems:"flex-start",marginBottom:10 }}>
              <div>
                <div style={{ display:"flex",alignItems:"center",gap:8,marginBottom:4 }}>
                  <PriorityDot priority={k.priority} t={t}/>
                  <span style={{ fontSize:13,fontWeight:800,color:t.text }}>{k.issue}</span>
                </div>
                <div style={{ fontSize:12,color:t.muted }}>{k.cat} · {k.date}</div>
              </div>
              <StatusBadge status={k.status} t={t}/>
            </div>
            <div style={{ display:"flex",gap:6,flexWrap:"wrap" }}>
              {[["pending","1. Tiếp nhận"],["in_progress","2. Đang sửa"],["done","3. Hoàn thành"]].map(([s,l])=>(
                <div key={s} style={{ fontSize:11,padding:"3px 10px",borderRadius:20,background:k.status===s?(s==="done"?t.greenDim:s==="in_progress"?t.blueDim:t.orangeDim):t.bg3,color:k.status===s?(s==="done"?t.green:s==="in_progress"?t.blue:t.orange):t.muted,fontWeight:k.status===s?700:400 }}>{l}</div>
              ))}
            </div>
          </Card>
        ))
      }
    </div>
  );
}

function TenantInvoiceTab({ room, t }) {
  const bill = calcBill(room);
  const month = "Tháng 6/2025";
  return (
    <div>
      <Card t={t} p="20px">
        <div style={{ textAlign:"center",marginBottom:20 }}>
          <div style={{ fontSize:18,fontWeight:900,color:t.accent }}>🏡 The Rental Hub</div>
          <div style={{ fontSize:13,fontWeight:700,color:t.text,marginTop:4 }}>HÓA ĐƠN TIỀN THUÊ</div>
          <div style={{ fontSize:12,color:t.muted }}>Phòng {room.id} · {month}</div>
        </div>
        <div style={{ display:"flex",justifyContent:"center",marginBottom:16 }}>
          <QRCode value={`phong-${room.id}-t6-2025-${bill.total}`} size={110}/>
        </div>
        <div style={{ fontSize:11,color:t.muted,textAlign:"center",marginBottom:20 }}>
          Quét mã để thanh toán qua MoMo / ZaloPay
        </div>

        {[
          ["🏠 Tiền phòng","Cố định mỗi tháng",fmt(bill.rent)],
          [`⚡ Điện (${bill.kWh} kWh × ${ELEC_PRICE.toLocaleString("vi-VN")}₫)`,`Chỉ số: ${room.electricOld} → ${room.electricNew}`,fmt(bill.electric)],
          [`💧 Nước (${room.water} m³ × ${WATER_PRICE.toLocaleString("vi-VN")}₫)`,"Tiêu thụ tháng",fmt(bill.water)],
          ["🛎 Phí dịch vụ","Internet + Vệ sinh + An ninh",fmt(bill.service)],
        ].map(([k,sub,v])=>(
          <div key={k} style={{ display:"flex",justifyContent:"space-between",alignItems:"center",padding:"12px 0",borderBottom:`1px dashed ${t.border}` }}>
            <div>
              <div style={{ fontSize:13,fontWeight:600,color:t.text }}>{k}</div>
              <div style={{ fontSize:11,color:t.muted }}>{sub}</div>
            </div>
            <div style={{ fontSize:14,fontWeight:700,color:t.text }}>{v}</div>
          </div>
        ))}

        <div style={{ display:"flex",justifyContent:"space-between",alignItems:"center",padding:"16px 0 0" }}>
          <div style={{ fontSize:16,fontWeight:800,color:t.text }}>TỔNG CỘNG</div>
          <div style={{ fontSize:24,fontWeight:900,color:t.accent }}>{fmt(bill.total)}</div>
        </div>

        <div style={{ marginTop:16,padding:"12px 16px",borderRadius:12,background:room.paid?t.greenDim:t.redDim,fontSize:14,fontWeight:800,color:room.paid?t.green:t.red,textAlign:"center" }}>
          {room.paid?"✅ ĐÃ THANH TOÁN":"⚠️ CHƯA THANH TOÁN — Hạn: 15/06/2025"}
        </div>
      </Card>

      <Card t={t} p="16px" mb={0}>
        <SectionTitle t={t}>🏦 Thông tin thanh toán</SectionTitle>
        {[["Ngân hàng","Vietcombank"],["Chủ TK","NGUYEN THANH LONG"],["STK","1234 5678 9012"],["Nội dung",`${room.id} T6 2025`]].map(([k,v])=>(
          <div key={k} style={{ display:"flex",justifyContent:"space-between",padding:"9px 0",borderBottom:`1px solid ${t.border}` }}>
            <span style={{ fontSize:12,color:t.muted }}>{k}</span>
            <span style={{ fontSize:13,fontWeight:700,color:k==="STK"||k==="Nội dung"?t.accent:t.text }}>{v}</span>
          </div>
        ))}
      </Card>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   LOGIN SCREEN
══════════════════════════════════════════════════════════════ */
function LoginScreen({ t, dark, setDark, onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [showPw, setShowPw] = useState(false);

  const tryLogin = () => {
    const u = USERS.find(u=>u.email===email&&u.password===password);
    if (u) { setError(""); onLogin(u); }
    else setError("Email hoặc mật khẩu không đúng!");
  };

  const quickLogin = (role) => {
    const u = USERS.find(u=>u.role===role);
    if (u) onLogin(u);
  };

  return (
    <div style={{ minHeight:"100vh",background:t.bg,display:"flex",flexDirection:"column",alignItems:"center",justifyContent:"center",fontFamily:"'Segoe UI',system-ui,sans-serif",padding:20 }}>
      <div style={{ textAlign:"center",marginBottom:36 }}>
        <div style={{ fontSize:60,marginBottom:12,filter:"drop-shadow(0 4px 12px rgba(245,158,11,0.4))" }}>🏡</div>
        <h1 style={{ margin:0,fontSize:32,fontWeight:900,color:t.text,letterSpacing:-1 }}>The Rental Hub</h1>
        <p style={{ color:t.muted,margin:"8px 0 0",fontSize:15 }}>Hệ sinh thái quản lý phòng trọ toàn diện</p>
      </div>

      <div style={{ width:"100%",maxWidth:400,background:t.bg2,borderRadius:20,border:`1px solid ${t.border}`,padding:"28px 28px",boxShadow:"0 20px 50px rgba(0,0,0,0.3)",marginBottom:24 }}>
        <div style={{ marginBottom:20 }}>
          <label style={{ fontSize:12,color:t.muted,display:"block",marginBottom:6,fontWeight:700 }}>EMAIL</label>
          <Input value={email} onChange={e=>setEmail(e.target.value)} placeholder="your@email.com" t={t} type="email"/>
        </div>
        <div style={{ marginBottom:20 }}>
          <label style={{ fontSize:12,color:t.muted,display:"block",marginBottom:6,fontWeight:700 }}>MẬT KHẨU</label>
          <div style={{ position:"relative" }}>
            <Input value={password} onChange={e=>setPassword(e.target.value)} placeholder="••••••••" t={t} type={showPw?"text":"password"}/>
            <button onClick={()=>setShowPw(!showPw)} style={{ position:"absolute",right:10,top:"50%",transform:"translateY(-50%)",background:"none",border:"none",color:t.muted,cursor:"pointer",fontSize:14 }}>
              {showPw?"🙈":"👁"}
            </button>
          </div>
        </div>
        {error&&<div style={{ padding:"8px 12px",background:t.redDim,borderRadius:8,fontSize:12,color:t.red,fontWeight:600,marginBottom:16 }}>⚠️ {error}</div>}
        <Btn t={t} onClick={tryLogin} full disabled={!email||!password}>🔑 Đăng nhập</Btn>
      </div>

      <div style={{ width:"100%",maxWidth:400 }}>
        <div style={{ fontSize:12,color:t.muted,textAlign:"center",marginBottom:12,fontWeight:700,letterSpacing:1 }}>— ĐĂNG NHẬP NHANH (DEMO) —</div>
        <div style={{ display:"flex",gap:10 }}>
          {[
            { role:"admin", icon:"🛡", label:"Admin", color:t.red, sub:"Toàn quyền hệ thống" },
            { role:"host",  icon:"👔", label:"Chủ trọ", color:t.accent, sub:"Quản lý tòa Sơn Trà" },
            { role:"tenant",icon:"🧑", label:"Khách thuê", color:t.blue, sub:"Phòng P102 — Trần Thị Bình" },
          ].map(({role,icon,label,color,sub})=>(
            <button key={role} onClick={()=>quickLogin(role)} style={{ flex:1,padding:"14px 10px",borderRadius:14,border:`2px solid ${t.border}`,background:t.bg2,cursor:"pointer",textAlign:"center",transition:"all 0.2s" }}
              onMouseEnter={e=>{e.currentTarget.style.borderColor=color;e.currentTarget.style.background=color+"15";}}
              onMouseLeave={e=>{e.currentTarget.style.borderColor=t.border;e.currentTarget.style.background=t.bg2;}}>
              <div style={{ fontSize:26,marginBottom:6 }}>{icon}</div>
              <div style={{ fontSize:13,fontWeight:800,color:t.text }}>{label}</div>
              <div style={{ fontSize:10,color:t.muted,marginTop:3,lineHeight:1.4 }}>{sub}</div>
            </button>
          ))}
        </div>
      </div>

      <div style={{ marginTop:24,display:"flex",flexWrap:"wrap",gap:8,justifyContent:"center",maxWidth:480 }}>
        {["📊 Dashboard trực quan","🏢 Sơ đồ tầng màu sắc","🧾 Hóa đơn tự động","🔧 Quản lý bảo trì","📦 Kiểm kê tài sản","⭐ Đánh giá khách thuê","📱 Mã QR thanh toán","🔐 Phân quyền 3 tầng"].map(f=>(
          <span key={f} style={{ fontSize:11,padding:"5px 11px",borderRadius:20,background:t.bg3,color:t.muted,border:`1px solid ${t.border}` }}>{f}</span>
        ))}
      </div>

      <button onClick={()=>setDark(!dark)} style={{ marginTop:20,fontSize:12,padding:"8px 16px",borderRadius:8,border:`1px solid ${t.border}`,background:t.bg3,color:t.muted,cursor:"pointer" }}>
        {dark?"☀️ Chế độ sáng":"🌙 Chế độ tối"}
      </button>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════
   ROOT
══════════════════════════════════════════════════════════════ */
export default function RentalHub() {
  const [dark, setDark] = useState(true);
  const [user, setUser] = useState(null);
  const t = useTheme(dark);

  const handleLogout = () => setUser(null);

  if (!user) return <LoginScreen t={t} dark={dark} setDark={setDark} onLogin={setUser}/>;
  if (user.role==="admin") return <AdminDashboard t={t} dark={dark} setDark={setDark} onLogout={handleLogout} user={user}/>;
  if (user.role==="host")  return <OwnerDashboard t={t} dark={dark} setDark={setDark} onLogout={handleLogout} user={user}/>;
  return <TenantPortal t={t} dark={dark} setDark={setDark} onLogout={handleLogout} user={user}/>;
}