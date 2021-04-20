qh = [-90:0,0:90];% 朝向.
meth = 0:90;% 已轉角度.
ql = 50;qw = 35;% 車體之半長、半寬.
tgw = 98;gtw = 110;gtl = 32+tgw*2;% 階寬、間墀寬、間墀長.
tgStart = 200;% 階長.
gl = 32;gw = 23;% 隅管.
coners = [-16,0,1;16,0,1;gtl/2-gw,gl-gtw,1;gw-gtl/2,gl-gtw,1]';
ss = length(qh);
ng_k = struct('x',zeros(ss,1),'y',zeros(ss,1));
figure();
fill([-150,-150,150,150],[-150,tgStart,tgStart,-150],[.9 .9 .9]);hold on;
plot(gtl/2*[1,1,-1,-1],[tgStart,-gtw,-gtw,tgStart],'LineWidth',1);
fill(gtl/2*[1,1,-1,-1],[tgStart,-gtw,-gtw,tgStart],[1,1,1]);
fill((gtl/2-tgw)*[1,1,-1,-1],[tgStart,0,0,tgStart],[.9 .9 .9]);
fill([gw,gw,0,0]-gtl/2, [0,gl,gl,0]-gtw, [.9 .9 .9]);
fill(gtl/2-[gw,gw,0,0], [0,gl,gl,0]-gtw, [.9 .9 .9]);
plot((gtl/2-qw)*[1,1,-1,-1],[tgStart,-gtw+qw,-gtw+qw,tgStart],'g--','LineWidth',1);
plot((gtl/2-tgw+qw)*[1,1,-1,-1],[tgStart,-qw,-qw,tgStart],'g--','LineWidth',1);
for st = 1:ss
    if st <= ss/2
        ng_k.x(st) = gtl/2 - sqrt(ql^2+qw^2)*sind(atand(qw/ql)+qh(st)+90);
        ng_k.y(st) = sqrt(ql^2+qw^2)*sind(90-qh(st)-90+atand(qw/ql)) - gtw;
    else
        ng_k.x(st) = -gtl/2 + sqrt(ql^2+qw^2)*cosd(qh(st)-atand(qw/ql));
        ng_k.y(st) = sqrt(ql^2+qw^2)*sind(qh(st)+atand(qw/ql)) - gtw;
    end
    if qh(st) == -45 || qh(st) == 45
        si_ng = [sind(qh(st)),-cosd(qh(st)),ng_k.x(st);cosd(qh(st)),sind(qh(st)),ng_k.y(st);0,0,1]*[qw,-qw,-qw,qw;ql,ql,-ql,-ql;1,1,1,1];
        si_ng = si_ng(1:2,:);% 車之四隅.
        fill(si_ng(1,:),si_ng(2,:),[.95 .95 .95]);
        plot(ng_k.x(st),ng_k.y(st),'mo');
    end
end
discrt = struct('x',-gtl/2 : gtl/2,'y',-gtw : 0);
%[dctPMx,dctPMy] = meshgrid(discrt.x,discrt.y);
fsbMap = true(length(discrt.y),length(discrt.x));
fsbMap(:,1:qw) = false;fsbMap(:,end-qw+1:end) = false;fsbMap(1:qw,:) = false;
%lbCorner = [-gtl/2,-gtw];
plot(ng_k.x,ng_k.y,'Color',[.1 .7 .1],'LineWidth',2);
plot((gtl/2-qw)*[1,1],[tgStart,ng_k.y(1)],'Color',[.1 .7 .1],'LineWidth',1);
plot((gtl/2-qw)*[-1,-1],[ng_k.y(end),tgStart],'Color',[.1 .7 .1],'LineWidth',1);
fsbArr = cell(length(discrt.y),length(discrt.x));fsbArrPI = cell(length(discrt.y),length(discrt.x));
fsbLen = zeros(length(discrt.y),length(discrt.x));
oaBE = [-180,90];
for x = 1:length(discrt.x)
    xc = x-gtl/2-1;
    if xc < 0
        oaBE = [-90,180];
    else
        oaBE = [-180,90];
    end
    for y = 1:length(discrt.y)
        yc = y-gtw-1;
        angleOK = false;
        for ornt = oaBE(1):oaBE(2)
            trans_q_o = [sind(ornt),-cosd(ornt),xc;cosd(ornt),sind(ornt),yc;0,0,1];
            si_ng = trans_q_o*[qw,-qw,-qw,qw;ql,ql,-ql,-ql;1,1,1,1];
            si_ng = si_ng(1:2,:);% 車之四隅坐標.
            cnrsLoc = trans_q_o\coners;% 隅在車坐標系之坐標.
            if any(abs(si_ng(1,:))>=gtl/2) || any(si_ng(2,:)<=-gtw) || sum(abs(si_ng(1,:))<=16 & si_ng(2,:)>=0)>0 || sum(abs(si_ng(1,:))>=gtl/2-gw & si_ng(2,:)<=gl-gtw)>0 || sum(abs(cnrsLoc(1,:))<=qw & abs(cnrsLoc(2,:))<=ql)>0% 四隅碰壁或車邊觸隅.
                if angleOK
                    angleOK = false;
                    fsbArr{y,x} = [fsbArr{y,x},ornt-1];% 可朝區間終（第偶數）.
                end
            else
                if ~angleOK
                    angleOK = true;
                    fsbArr{y,x} = [fsbArr{y,x},ornt];% 可朝區間始（第奇數）.
                end
            end
        end
        if angleOK
            fsbArr{y,x} = [fsbArr{y,x},oaBE(2)];
        end
        fsbLen(y,x) = sum(fsbArr{y,x}*diag(repmat([-1,1],1,length(fsbArr{y,x})/2)));
        fsbArrPI{y,x} = fsbArr{y,x}*pi/180;
%         if fsbMap(y,x)
%             plot(xc, yc, '*g');
%         else
%             plot(xc, yc, 'xr');
%         end
    end
end
mesh(discrt.x,discrt.y,fsbLen);
set(gca,'DataAspectRatio',[1 1 1]);
hold off;
xlabel('x');ylabel('y: stair');